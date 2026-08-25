// ============================================================================
// CONFIG
// ============================================================================
const CONFIG = {
  mapWidth: 10000, mapHeight: 10000, vertexCount: 10000, seed: 20260405,
  extraEdgeProb: 0.28,
  shortRoadCap: 30, mediumRoadCap: 50, longRoadCap: 70,
  trafficAlpha: 0.5, trafficC: 0.05,
  spawnPerTick: 80, maxVehicles: 10000,
  simIntervalMs: 200
};

// ============================================================================
// GEOMETRY UTILS
// ============================================================================
function dist(x1, y1, x2, y2) { return Math.sqrt((x1-x2)**2 + (y1-y2)**2); }
function distSq(x1, y1, x2, y2) { return (x1-x2)**2 + (y1-y2)**2; }

// ============================================================================
// GRAPH MODEL
// ============================================================================
class Vertex {
  constructor(id, x, y, name, type) {
    this.id = id; this.x = x; this.y = y; this.name = name; this.type = type;
    this.edgeIds = [];
  }
}
class Edge {
  constructor(id, fromId, toId, length, capacity, vehicles = 0) {
    this.id = id; this.fromId = fromId; this.toId = toId;
    this.length = length; this.capacity = capacity; this.vehicles = vehicles;
  }
  getOther(vid) { return vid === this.fromId ? this.toId : this.fromId; }
  get occupancy() { return this.capacity <= 0 ? 0 : this.vehicles / this.capacity; }
  travelTime(alpha, c) {
    const r = this.occupancy;
    const factor = r <= alpha ? 1.0 : 1.0 + Math.exp(r - alpha);
    return c * this.length * factor;
  }
}
class Graph {
  constructor() {
    this.vertices = new Map();
    this.edges = new Map();
    this.adjacency = new Map();
  }
  addVertex(v) {
    this.vertices.set(v.id, v);
    if (!this.adjacency.has(v.id)) this.adjacency.set(v.id, []);
  }
  addEdge(e) {
    this.edges.set(e.id, e);
    if (!this.adjacency.has(e.fromId)) this.adjacency.set(e.fromId, []);
    if (!this.adjacency.has(e.toId)) this.adjacency.set(e.toId, []);
    this.adjacency.get(e.fromId).push(e);
    this.adjacency.get(e.toId).push(e);
    const f = this.vertices.get(e.fromId);
    const t = this.vertices.get(e.toId);
    if (f) f.edgeIds.push(e.id);
    if (t) t.edgeIds.push(e.id);
  }
  clearTraffic() { for (const e of this.edges.values()) e.vehicles = 0; }
  getBounds() {
    let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
    for (const v of this.vertices.values()) {
      minX = Math.min(minX, v.x); minY = Math.min(minY, v.y);
      maxX = Math.max(maxX, v.x); maxY = Math.max(maxY, v.y);
    }
    return { minX, minY, maxX, maxY, w: Math.max(1, maxX-minX), h: Math.max(1, maxY-minY) };
  }
}

// ============================================================================
// MAP GENERATOR
// ============================================================================
function generateMap(vertexCount, seed) {
  const graph = new Graph();
  const rng = mulberry32(seed);
  const types = ['普通地点','加油站','餐馆','停车场','维修点'];
  const rows = Math.ceil(Math.sqrt(vertexCount));
  const cols = Math.ceil(vertexCount / rows);
  const cellW = CONFIG.mapWidth / cols;
  const cellH = CONFIG.mapHeight / rows;
  const jitter = 0.22;
  const gridIds = [];

  let vid = 0;
  for (let row = 0; row < rows; row++) {
    gridIds[row] = [];
    for (let col = 0; col < cols; col++) {
      if (vid >= vertexCount) { gridIds[row][col] = -1; continue; }
      const cx = (col + 0.5) * cellW;
      const cy = (row + 0.5) * cellH;
      const x = clamp(cx + (rng()*2-1) * cellW * jitter, 0, CONFIG.mapWidth);
      const y = clamp(cy + (rng()*2-1) * cellH * jitter, 0, CONFIG.mapHeight);
      graph.addVertex(new Vertex(vid, x, y, `P${String(vid).padStart(5,'0')}`, types[Math.floor(rng()*types.length)]));
      gridIds[row][col] = vid;
      vid++;
    }
  }

  let eid = 0;
  const edgeKeys = new Set();
  for (let row = 0; row < rows; row++) {
    for (let col = 0; col < cols; col++) {
      const cur = gridIds[row][col];
      if (cur < 0) continue;
      if (col+1 < cols && gridIds[row][col+1] >= 0) eid = addEdge(graph, edgeKeys, eid, cur, gridIds[row][col+1], rng);
      if (row+1 < rows && gridIds[row+1][col] >= 0) eid = addEdge(graph, edgeKeys, eid, cur, gridIds[row+1][col], rng);
      if (row+1 < rows && col+1 < cols && gridIds[row+1][col] >= 0 && gridIds[row][col+1] >= 0 && gridIds[row+1][col+1] >= 0) {
        if (rng() < CONFIG.extraEdgeProb) {
          if (rng() < 0.5) eid = addEdge(graph, edgeKeys, eid, cur, gridIds[row+1][col+1], rng);
          else eid = addEdge(graph, edgeKeys, eid, gridIds[row][col+1], gridIds[row+1][col], rng);
        }
      }
    }
  }
  return graph;
}

function addEdge(graph, edgeKeys, eid, fromId, toId, rng) {
  const a = Math.min(fromId, toId), b = Math.max(fromId, toId);
  const key = `${a}-${b}`;
  if (edgeKeys.has(key)) return eid;
  edgeKeys.add(key);
  const from = graph.vertices.get(fromId), to = graph.vertices.get(toId);
  const len = dist(from.x, from.y, to.x, to.y);
  const cap = len < 110 ? CONFIG.shortRoadCap + Math.floor(rng()*8)
            : len < 155 ? CONFIG.mediumRoadCap + Math.floor(rng()*10)
            : CONFIG.longRoadCap + Math.floor(rng()*12);
  graph.addEdge(new Edge(eid, fromId, toId, len, cap));
  return eid + 1;
}

function mulberry32(a) {
  return function() {
    a |= 0; a = a + 0x6D2B79F5 | 0;
    let t = Math.imul(a ^ a >>> 15, 1 | a);
    t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t;
    return ((t ^ t >>> 14) >>> 0) / 4294967296;
  };
}
function clamp(v, min, max) { return Math.max(min, Math.min(max, v)); }

// ============================================================================
// KD-TREE
// ============================================================================
class KDTree {
  constructor() { this.root = null; }
  build(vertices) {
    const arr = [...vertices];
    this.root = this._build(arr, 0);
  }
  _build(arr, depth) {
    if (arr.length === 0) return null;
    const axis = depth % 2;
    arr.sort((a,b) => axis === 0 ? a.x - b.x : a.y - b.y);
    const mid = Math.floor(arr.length / 2);
    const node = { vertex: arr[mid], axis, left: null, right: null };
    node.left = this._build(arr.slice(0, mid), depth + 1);
    node.right = this._build(arr.slice(mid + 1), depth + 1);
    return node;
  }
  findNearest(x, y) {
    return this._nearest(this.root, x, y, null).vertex;
  }
  _nearest(node, x, y, best) {
    if (!node) return best;
    const d2 = distSq(node.vertex.x, node.vertex.y, x, y);
    const cur = { vertex: node.vertex, d2 };
    if (!best || cur.d2 < best.d2) best = cur;
    const delta = node.axis === 0 ? x - node.vertex.x : y - node.vertex.y;
    const first = delta < 0 ? node.left : node.right;
    const second = delta < 0 ? node.right : node.left;
    best = this._nearest(first, x, y, best);
    if (delta * delta < best.d2) best = this._nearest(second, x, y, best);
    return best;
  }
  findKNearest(x, y, k) {
    const heap = [];
    this._knearest(this.root, x, y, k, heap);
    heap.sort((a,b) => a.d2 - b.d2);
    return heap.map(h => h.vertex);
  }
  _knearest(node, x, y, k, heap) {
    if (!node) return;
    const d2 = distSq(node.vertex.x, node.vertex.y, x, y);
    const cur = { vertex: node.vertex, d2 };
    if (heap.length < k) {
      heap.push(cur);
      heap.sort((a,b) => b.d2 - a.d2);
    } else if (d2 < heap[0].d2) {
      heap[0] = cur;
      heap.sort((a,b) => b.d2 - a.d2);
    }
    const delta = node.axis === 0 ? x - node.vertex.x : y - node.vertex.y;
    const first = delta < 0 ? node.left : node.right;
    const second = delta < 0 ? node.right : node.left;
    this._knearest(first, x, y, k, heap);
    if (heap.length < k || delta * delta < heap[0].d2) {
      this._knearest(second, x, y, k, heap);
    }
  }
}

// ============================================================================
// A* PATHFINDER
// ============================================================================
class PathFinder {
  constructor(alpha, c) { this.alpha = alpha; this.c = c; this.graph = null; }
  setGraph(g) { this.graph = g; }
  findShortestByDist(startId, endId) { return this._find(startId, endId, false); }
  findShortestByTime(startId, endId) { return this._find(startId, endId, true); }

  _find(startId, endId, useTime) {
    if (startId === endId) return { vertexIds: [startId], edgeIds: [], totalDist: 0, totalTime: 0 };
    const g = this.graph;
    const start = g.vertices.get(startId), goal = g.vertices.get(endId);
    if (!start || !goal) throw new Error('起点或终点不存在');

    const openSet = new MinHeap();
    const gScore = new Map();
    const prevV = new Map(), prevE = new Map();

    gScore.set(startId, 0);
    openSet.push(startId, this._heuristic(start, goal, useTime));

    while (openSet.size > 0) {
      const curId = openSet.pop();
      if (curId === endId) return this._reconstruct(prevV, prevE, endId);

      for (const edge of g.adjacency.get(curId) || []) {
        const nbId = edge.getOther(curId);
        const w = useTime ? edge.travelTime(this.alpha, this.c) : edge.length;
        const tent = gScore.get(curId) + w;
        if (tent < (gScore.get(nbId) ?? Infinity)) {
          prevV.set(nbId, curId);
          prevE.set(nbId, edge.id);
          gScore.set(nbId, tent);
          const f = tent + this._heuristic(g.vertices.get(nbId), goal, useTime);
          openSet.push(nbId, f);
        }
      }
    }
    throw new Error('未找到可达路径');
  }
  _heuristic(a, b, useTime) {
    const d = dist(a.x, a.y, b.x, b.y);
    return useTime ? d * this.c : d;
  }
  _reconstruct(prevV, prevE, endId) {
    const vids = [], eids = [];
    let cur = endId;
    vids.push(cur);
    while (prevV.has(cur)) {
      eids.push(prevE.get(cur));
      cur = prevV.get(cur);
      vids.push(cur);
    }
    vids.reverse(); eids.reverse();
    let td = 0, tt = 0;
    for (const eid of eids) {
      const e = this.graph.edges.get(eid);
      td += e.length;
      tt += e.travelTime(this.alpha, this.c);
    }
    return { vertexIds: vids, edgeIds: eids, totalDist: td, totalTime: tt };
  }
}

class MinHeap {
  constructor() { this.data = []; }
  get size() { return this.data.length; }
  push(id, f) { this.data.push({id,f}); this._up(this.data.length-1); }
  pop() {
    const top = this.data[0];
    const last = this.data.pop();
    if (this.data.length > 0) { this.data[0] = last; this._down(0); }
    return top.id;
  }
  _up(i) {
    while (i > 0) {
      const p = (i-1)>>1;
      if (this.data[i].f < this.data[p].f) { [this.data[i],this.data[p]]=[this.data[p],this.data[i]]; i=p; }
      else break;
    }
  }
  _down(i) {
    const n = this.data.length;
    while (true) {
      let sm = i, l = i*2+1, r = i*2+2;
      if (l < n && this.data[l].f < this.data[sm].f) sm = l;
      if (r < n && this.data[r].f < this.data[sm].f) sm = r;
      if (sm !== i) { [this.data[i],this.data[sm]]=[this.data[sm],this.data[i]]; i=sm; }
      else break;
    }
  }
}

// ============================================================================
// TRAFFIC SIMULATION
// ============================================================================
class TrafficSim {
  constructor(pathFinder) {
    this.pf = pathFinder;
    this.graph = null;
    this.running = false;
    this.vehicles = [];
    this.nextId = 0;
  }
  setGraph(g) { this.graph = g; this.reset(); }
  start() { this.running = true; }
  pause() { this.running = false; }
  reset() {
    this.running = false; this.vehicles = [];
    this.nextId = 0;
    if (this.graph) this.graph.clearTraffic();
  }
  tick() {
    if (!this.running || !this.graph || this.graph.vertices.size === 0) return;
    const g = this.graph;
    if (this.vehicles.length < CONFIG.maxVehicles) {
      for (let i = 0; i < CONFIG.spawnPerTick; i++) this._spawn();
    }
    for (let i = this.vehicles.length-1; i >= 0; i--) {
      const v = this.vehicles[i];
      v.remaining--;
      while (v.active && v.remaining <= 0) {
        const finEid = v.edgeIds[v.edgeIdx];
        g.edges.get(finEid).vehicles = Math.max(0, g.edges.get(finEid).vehicles - 1);
        v.edgeIdx++;
        if (v.edgeIdx >= v.edgeIds.length) { v.active = false; break; }
        const nextE = g.edges.get(v.edgeIds[v.edgeIdx]);
        nextE.vehicles++;
        v.edgeDuration = Math.max(1, nextE.travelTime(CONFIG.trafficAlpha, CONFIG.trafficC));
        v.remaining += v.edgeDuration;
      }
      if (!v.active) this.vehicles.splice(i, 1);
    }
  }
  _spawn() {
    const g = this.graph;
    const n = g.vertices.size;
    if (n < 2) return;
    let s = Math.floor(Math.random()*n), e = Math.floor(Math.random()*n);
    while (s === e) e = Math.floor(Math.random()*n);
    try {
      const path = this.pf.findShortestByDist(s, e);
      if (path.edgeIds.length === 0) return;
      const fe = g.edges.get(path.edgeIds[0]);
      const dur = Math.max(1, fe.travelTime(CONFIG.trafficAlpha, CONFIG.trafficC));
      fe.vehicles++;
      this.vehicles.push({
        id: this.nextId++, vertexIds: path.vertexIds, edgeIds: path.edgeIds,
        edgeIdx: 0, remaining: dur, edgeDuration: dur, active: true
      });
    } catch(ex) {}
  }
  getRenderStates() {
    return this.vehicles.filter(v => v.active && v.edgeIdx + 1 < v.vertexIds.length).map(v => {
      const dur = Math.max(1, v.edgeDuration);
      return {
        fromId: v.vertexIds[v.edgeIdx], toId: v.vertexIds[v.edgeIdx + 1],
        progress: clamp(1 - v.remaining / dur, 0, 1)
      };
    });
  }
}

// ============================================================================
// MAP RENDERER
// ============================================================================
const COLORS = {
  roadDefault: '#D5D8DD', roadQuery: '#1A6BC0', roadDist: '#2EA846', roadTime: '#D6362E',
  pointDefault: '#555A63', pointStart: '#2EA846', pointEnd: '#D6362E',
  trafficSmooth: '#2EA846', trafficModerate: '#ECC423', trafficCongested: '#D6362E',
};

function roadWidthByLength(len) {
  if (len >= 150) return 2.8;
  if (len >= 100) return 1.8;
  return 1.0;
}

function trafficColor(ratio) {
  if (ratio <= 0.5) return COLORS.trafficSmooth;
  if (ratio <= 0.9) return COLORS.trafficModerate;
  return COLORS.trafficCongested;
}

class MapRenderer {
  constructor(canvas) { this.canvas = canvas; this.ctx = canvas.getContext('2d'); }

  resize(w, h) {
    const dpr = window.devicePixelRatio || 1;
    this.canvas.width = w * dpr;
    this.canvas.height = h * dpr;
    this.canvas.style.width = w + 'px';
    this.canvas.style.height = h + 'px';
    this.ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  }

  render(state) {
    const ctx = this.ctx;
    const w = this.canvas.width / (window.devicePixelRatio||1);
    const h = this.canvas.height / (window.devicePixelRatio||1);
    ctx.clearRect(0, 0, w, h);
    ctx.fillStyle = '#F5F6FA';
    ctx.fillRect(0, 0, w, h);

    if (!state.graph || state.graph.vertices.size === 0) {
      ctx.fillStyle = '#888'; ctx.font = '14px "Microsoft YaHei", sans-serif';
      ctx.fillText('暂无地图数据，请先生成或加载地图。', 30, 30);
      return;
    }

    const worldRect = this._worldRect(state, w, h);
    const visible = this._visibleVertices(state, worldRect, w, h);
    const visSet = new Set(visible.map(v => v.id));

    this._drawEdges(ctx, state, worldRect, visSet, w, h);
    this._drawVertices(ctx, state, worldRect, visible, w, h);
    if (state.showTraffic) this._drawVehicles(ctx, state, worldRect);
  }

  _worldRect(state, w, h) {
    const hw = w / state.scale / 2, hh = h / state.scale / 2;
    return { minX: state.cx - hw, maxX: state.cx + hw, minY: state.cy - hh, maxY: state.cy + hh, w: w/state.scale, h: h/state.scale };
  }

  _visibleVertices(state, wr, w, h) {
    const cellPx = state.scale >= 0.5 ? 0 : state.scale >= 0.15 ? 24 : 40;
    if (cellPx === 0) {
      return [...state.graph.vertices.values()].filter(v => v.x >= wr.minX && v.x <= wr.maxX && v.y >= wr.minY && v.y <= wr.maxY);
    }
    const sampled = new Map();
    for (const v of state.graph.vertices.values()) {
      if (v.x < wr.minX || v.x > wr.maxX || v.y < wr.minY || v.y > wr.maxY) continue;
      const px = Math.floor((v.x - wr.minX) / wr.w * w);
      const py = Math.floor((wr.maxY - v.y) / wr.h * h);
      const cx = Math.max(0, Math.floor(px / cellPx));
      const cy = Math.max(0, Math.floor(py / cellPx));
      const key = `${cx}-${cy}`;
      if (!sampled.has(key)) sampled.set(key, v);
    }
    return [...sampled.values()];
  }

  _w2sx(state, wx) { return (wx - state.cx) * state.scale + this.canvas.width/(window.devicePixelRatio||1)/2; }
  _w2sy(state, wy) { return this.canvas.height/(window.devicePixelRatio||1)/2 - (wy - state.cy) * state.scale; }

  _drawEdges(ctx, state, wr, visSet, w, h) {
    const queryE = state.queryEdgeIds, distE = state.distEdgeIds, timeE = state.timeEdgeIds;
    for (const edge of state.graph.edges.values()) {
      const hl = queryE.has(edge.id) || distE.has(edge.id) || timeE.has(edge.id);
      if (!hl && (!visSet.has(edge.fromId) || !visSet.has(edge.toId))) continue;
      const f = state.graph.vertices.get(edge.fromId), t = state.graph.vertices.get(edge.toId);
      const sx1 = this._w2sx(state, f.x), sy1 = this._w2sy(state, f.y);
      const sx2 = this._w2sx(state, t.x), sy2 = this._w2sy(state, t.y);
      let color = COLORS.roadDefault;
      const bw = roadWidthByLength(edge.length);
      let sw = bw;
      if (state.showTraffic && !hl) {
        color = trafficColor(edge.occupancy);
        sw = bw + Math.min(2.5, edge.occupancy * 3);
      }
      if (queryE.has(edge.id)) { color = COLORS.roadQuery; sw = bw + 0.8; }
      const isDist = distE.has(edge.id), isTime = timeE.has(edge.id);
      if (isDist || isTime) {
        color = isDist ? COLORS.roadDist : COLORS.roadTime;
        sw = bw + (isDist ? 1.6 : 1.8);
        ctx.strokeStyle = color + '22';
        ctx.lineWidth = sw + 5;
        ctx.lineCap = 'round'; ctx.lineJoin = 'round';
        ctx.beginPath(); ctx.moveTo(sx1, sy1); ctx.lineTo(sx2, sy2); ctx.stroke();
      }
      ctx.strokeStyle = color;
      ctx.lineWidth = sw;
      ctx.lineCap = 'round'; ctx.lineJoin = 'round';
      ctx.beginPath(); ctx.moveTo(sx1, sy1); ctx.lineTo(sx2, sy2); ctx.stroke();
    }
  }

  _drawVertices(ctx, state, wr, visible, w, h) {
    const qv = state.queryVertexIds, dv = state.distVertexIds, tv = state.timeVertexIds;
    for (const v of visible) {
      const sx = this._w2sx(state, v.x), sy = this._w2sy(state, v.y);
      let r = 3, color = COLORS.pointDefault;
      if (qv.has(v.id)) { color = COLORS.roadQuery; r = 5; }
      if (dv.has(v.id)) { color = COLORS.roadDist; r = 5; }
      if (tv.has(v.id)) { color = COLORS.roadTime; r = 5; }
      const isStart = state.startId != null && state.startId === v.id;
      const isEnd = state.endId != null && state.endId === v.id;
      if (isStart || isEnd) {
        ctx.fillStyle = isStart ? COLORS.pointStart : COLORS.pointEnd;
        ctx.beginPath(); ctx.arc(sx, sy, 7, 0, Math.PI*2); ctx.fill();
        ctx.fillStyle = '#FFFFFF';
        ctx.beginPath(); ctx.arc(sx, sy, 3, 0, Math.PI*2); ctx.fill();
      } else {
        ctx.fillStyle = color;
        ctx.beginPath(); ctx.arc(sx, sy, r, 0, Math.PI*2); ctx.fill();
      }
    }
  }

  _drawVehicles(ctx, state, wr) {
    ctx.fillStyle = 'rgba(26,107,192,0.85)';
    for (const vs of state.vehicles) {
      const f = state.graph.vertices.get(vs.fromId), t = state.graph.vertices.get(vs.toId);
      if (!f || !t) continue;
      const wx = f.x + (t.x - f.x) * vs.progress;
      const wy = f.y + (t.y - f.y) * vs.progress;
      const sx = this._w2sx(state, wx), sy = this._w2sy(state, wy);
      const ang = Math.atan2(-(t.y - f.y), t.x - f.x);
      ctx.save(); ctx.translate(sx, sy); ctx.rotate(ang);
      ctx.beginPath(); ctx.moveTo(5,0); ctx.lineTo(-3,-3); ctx.lineTo(-3,3); ctx.closePath(); ctx.fill();
      ctx.restore();
    }
  }

  _roundRect(ctx, x, y, w, h, r) {
    ctx.beginPath();
    ctx.moveTo(x+r, y); ctx.arcTo(x+w, y, x+w, y+h, r); ctx.arcTo(x+w, y+h, x, y+h, r);
    ctx.arcTo(x, y+h, x, y, r); ctx.arcTo(x, y, x+w, y, r);
    ctx.closePath();
  }
}

// ============================================================================
// APP CONTROLLER
// ============================================================================
class App {
  constructor() {
    this.graph = null;
    this.kdTree = new KDTree();
    this.pathFinder = new PathFinder(CONFIG.trafficAlpha, CONFIG.trafficC);
    this.traffic = new TrafficSim(this.pathFinder);
    this.renderer = new MapRenderer(document.getElementById('mapCanvas'));

    // View state
    this.cx = 5000; this.cy = 5000; this.scale = 0.05;
    this.dragging = false; this.dragX = 0; this.dragY = 0;
    this.hoverX = null; this.hoverY = null;

    // Selection state
    this.startId = null; this.endId = null;
    this.queryVertexIds = new Set(); this.queryEdgeIds = new Set();
    this.distVertexIds = new Set(); this.distEdgeIds = new Set();
    this.timeVertexIds = new Set(); this.timeEdgeIds = new Set();
    this.showTraffic = false;
    this.simInterval = null;

    this.init();
  }

  init() {
    this._buildNavMenus();
    this._bindButtons();
    this._bindCanvas();

    this._resize();
    window.addEventListener('resize', () => this._resize());

    // Generate initial map
    this._genMap(CONFIG.seed);
  }

  _buildNavMenus() {
    const menus = [
      ['文件', [['生成地图', ()=>this._genMap()], ['退出', ()=>{}]]],
      ['查询', [['附近100点', ()=>this._nearby()], ['最短路径', ()=>this._distPath()], ['路况最优路径', ()=>this._timePath()]]],
      ['模拟', [['开始', ()=>this._simStart()], ['暂停', ()=>this._simPause()], ['重置', ()=>this._simReset()]]],
      ['视图', [['重置视图', ()=>this._resetView()], ['清空高亮', ()=>this._clearHighlights()]]],
    ];
    const container = document.getElementById('navMenus');
    menus.forEach(([name, items]) => {
      const wrap = document.createElement('div'); wrap.className = 'nav-dropdown';
      const btn = document.createElement('button'); btn.className = 'nav-menu-btn'; btn.textContent = name;
      const menu = document.createElement('div'); menu.className = 'nav-dropdown-menu';
      items.forEach(([label, fn]) => {
        const item = document.createElement('button'); item.className = 'nav-dropdown-item';
        item.textContent = label; item.addEventListener('click', () => { menu.classList.remove('show'); fn(); });
        menu.appendChild(item);
      });
      btn.addEventListener('click', (e) => { e.stopPropagation(); this._closeAllMenus(); menu.classList.toggle('show'); });
      wrap.appendChild(btn); wrap.appendChild(menu); container.appendChild(wrap);
    });
    document.addEventListener('click', () => this._closeAllMenus());
  }
  _closeAllMenus() { document.querySelectorAll('.nav-dropdown-menu').forEach(m => m.classList.remove('show')); }

  _bindButtons() {
    document.getElementById('nearbyBtn').addEventListener('click', ()=>this._nearby());
    document.getElementById('distanceBtn').addEventListener('click', ()=>this._distPath());
    document.getElementById('timeBtn').addEventListener('click', ()=>this._timePath());
    document.getElementById('simStartBtn').addEventListener('click', ()=>this._simStart());
    document.getElementById('simPauseBtn').addEventListener('click', ()=>this._simPause());
    document.getElementById('simResetBtn').addEventListener('click', ()=>this._simReset());
    document.getElementById('genMapBtn').addEventListener('click', ()=>this._genMap());
    document.getElementById('resetViewBtn').addEventListener('click', ()=>this._resetView());
    document.getElementById('clearBtn').addEventListener('click', ()=>this._clearHighlights());
  }

  _bindCanvas() {
    const canvas = document.getElementById('mapCanvas');
    const tooltip = document.getElementById('edgeTooltip');

    canvas.addEventListener('mousedown', (e) => {
      this.dragging = true; this.dragX = e.clientX; this.dragY = e.clientY;
    });
    canvas.addEventListener('mousemove', (e) => {
      const rect = canvas.getBoundingClientRect();
      this.hoverX = e.clientX - rect.left; this.hoverY = e.clientY - rect.top;
      if (this.dragging) {
        const dx = e.clientX - this.dragX, dy = e.clientY - this.dragY;
        this.cx -= dx / this.scale;
        this.cy += dy / this.scale;
        this.dragX = e.clientX; this.dragY = e.clientY;
        this._repaint();
        return;
      }
      this._updateHoverTooltip(e.clientX, e.clientY);
      this._updateCoordOverlay();
      this._repaint();
    });
    canvas.addEventListener('mouseup', (e) => {
      if (this.dragging) { this.dragging = false; return; }
    });
    canvas.addEventListener('mouseleave', () => {
      this.dragging = false; this.hoverX = null; this.hoverY = null;
      tooltip.style.display = 'none';
      this._updateCoordOverlay();
      this._repaint();
    });
    canvas.addEventListener('click', (e) => {
      if (this.dragging) return;
      const rect = canvas.getBoundingClientRect();
      const sx = e.clientX - rect.left, sy = e.clientY - rect.top;
      const wx = (sx - rect.width/2) / this.scale + this.cx;
      const wy = (rect.height/2 - sy) / this.scale + this.cy;
      this._selectVertex(wx, wy);
    });
    canvas.addEventListener('wheel', (e) => {
      e.preventDefault();
      const rect = canvas.getBoundingClientRect();
      const sx = e.clientX - rect.left, sy = e.clientY - rect.top;
      const bx = (sx - rect.width/2) / this.scale + this.cx;
      const by = (rect.height/2 - sy) / this.scale + this.cy;
      const factor = e.deltaY < 0 ? 1.12 : 0.9;
      this.scale *= factor;
      this.scale = clamp(this.scale, 0.01, 40);
      const ax = (sx - rect.width/2) / this.scale + this.cx;
      const ay = (rect.height/2 - sy) / this.scale + this.cy;
      this.cx += bx - ax; this.cy += by - ay;
      this._updateCoordOverlay();
      this._repaint();
    }, { passive: false });
  }

  _updateHoverTooltip(cx, cy) {
    const tooltip = document.getElementById('edgeTooltip');
    if (!this.graph) { tooltip.style.display = 'none'; return; }
    const rect = document.getElementById('mapCanvas').getBoundingClientRect();
    const sx = cx - rect.left, sy = cy - rect.top;
    let best = null, bestD2 = 64;
    for (const edge of this.graph.edges.values()) {
      const f = this.graph.vertices.get(edge.fromId), t = this.graph.vertices.get(edge.toId);
      if (!f || !t) continue;
      const x1 = this._w2sx(f.x), y1 = this._w2sy(f.y);
      const x2 = this._w2sx(t.x), y2 = this._w2sy(t.y);
      const d2 = ptSegDistSq(x1, y1, x2, y2, sx, sy);
      if (d2 < bestD2) { bestD2 = d2; best = edge; }
    }
    if (best) {
      tooltip.style.display = 'block';
      tooltip.style.left = (cx - rect.left + 16) + 'px';
      tooltip.style.top = (cy - rect.top + 16) + 'px';
      tooltip.textContent = `道路 ${best.id} | 长度: ${best.length.toFixed(1)} | 容量: ${best.capacity} | 当前: ${best.vehicles} | 负载: ${best.occupancy.toFixed(2)}`;
    } else {
      tooltip.style.display = 'none';
    }
  }

  _updateCoordOverlay() {
    const el = document.getElementById('coordOverlay');
    let mx = '-', my = '-';
    if (this.hoverX != null) {
      const rect = document.getElementById('mapCanvas').getBoundingClientRect();
      mx = ((this.hoverX - rect.width/2) / this.scale + this.cx).toFixed(1);
      my = ((rect.height/2 - this.hoverY) / this.scale + this.cy).toFixed(1);
    }
    el.innerHTML = `鼠标: (${mx}, ${my})<br>中心: (${this.cx.toFixed(1)}, ${this.cy.toFixed(1)})<br>缩放: ${this.scale.toFixed(3)}`;
  }

  _w2sx(wx) { return (wx - this.cx) * this.scale + document.getElementById('mapCanvas').getBoundingClientRect().width/2; }
  _w2sy(wy) { return document.getElementById('mapCanvas').getBoundingClientRect().height/2 - (wy - this.cy) * this.scale; }

  _resize() {
    const container = document.getElementById('mapContainer');
    this.renderer.resize(container.clientWidth, container.clientHeight);
    this._repaint();
  }

  _repaint() {
    this.renderer.render({
      graph: this.graph,
      cx: this.cx, cy: this.cy, scale: this.scale,
      queryVertexIds: this.queryVertexIds, queryEdgeIds: this.queryEdgeIds,
      distVertexIds: this.distVertexIds, distEdgeIds: this.distEdgeIds,
      timeVertexIds: this.timeVertexIds, timeEdgeIds: this.timeEdgeIds,
      startId: this.startId, endId: this.endId,
      showTraffic: this.showTraffic,
      vehicles: this.showTraffic ? this.traffic.getRenderStates() : []
    });
  }

  _genMap(seed) {
    this._setStatus('正在生成地图...');
    setTimeout(() => {
      const s = seed || Date.now();
      this.graph = generateMap(CONFIG.vertexCount, s);
      this.kdTree.build(this.graph.vertices.values());
      this.pathFinder.setGraph(this.graph);
      this.traffic.setGraph(this.graph);
      this.startId = null; this.endId = null;
      this._clearHighlights();
      this._resetView();
      document.getElementById('startLabel').textContent = 'A 点：未选择';
      document.getElementById('endLabel').textContent = 'B 点：未选择';
      document.getElementById('resultArea').value = '当前地图已就绪。\n请点击地图选择 A 点和 B 点，或输入坐标执行附近查询。';
      this._setStatus(`地图生成成功，顶点数：${this.graph.vertices.size}，边数：${this.graph.edges.size}`);
      this._repaint();
    }, 50);
  }

  _resetView() {
    if (!this.graph) return;
    const b = this.graph.getBounds();
    const rect = document.getElementById('mapCanvas').getBoundingClientRect();
    const w = Math.max(rect.width, 900), h = Math.max(rect.height, 700);
    this.cx = (b.minX + b.maxX) / 2;
    this.cy = (b.minY + b.maxY) / 2;
    this.scale = Math.min(w / (b.w * 1.1), h / (b.h * 1.1));
    if (!isFinite(this.scale) || this.scale <= 0) this.scale = 0.05;
    this._updateCoordOverlay();
    this._repaint();
  }

  _clearHighlights() {
    this.queryVertexIds.clear(); this.queryEdgeIds.clear();
    this.distVertexIds.clear(); this.distEdgeIds.clear();
    this.timeVertexIds.clear(); this.timeEdgeIds.clear();
    document.getElementById('resultArea').value = '已清空当前高亮结果。';
    this._repaint();
  }

  _selectVertex(wx, wy) {
    if (!this.graph) return;
    try {
      const nearest = this.kdTree.findNearest(wx, wy);
      if (this.startId == null || this.endId != null) {
        this.startId = nearest.id; this.endId = null;
        this.distVertexIds.clear(); this.distEdgeIds.clear();
        this.timeVertexIds.clear(); this.timeEdgeIds.clear();
        document.getElementById('startLabel').textContent = 'A 点：' + nearest.name;
        document.getElementById('endLabel').textContent = 'B 点：未选择';
      } else if (nearest.id !== this.startId) {
        this.endId = nearest.id;
        document.getElementById('endLabel').textContent = 'B 点：' + nearest.name;
      }
      this._setStatus(`已选择地点：${nearest.name}（${nearest.type}）`);
      this._repaint();
    } catch(ex) { this._showToast(ex.message); }
  }

  _nearby() {
    if (!this.graph) return;
    try {
      const x = parseFloat(document.getElementById('xField').value);
      const y = parseFloat(document.getElementById('yField').value);
      if (isNaN(x) || isNaN(y)) throw new Error('请输入有效坐标');
      const b = this.graph.getBounds();
      if (x < b.minX || x > b.maxX || y < b.minY || y > b.maxY) throw new Error('坐标超出地图范围');
      const vertices = this.kdTree.findKNearest(x, y, 100);
      this.queryVertexIds.clear(); this.queryEdgeIds.clear();
      this.distVertexIds.clear(); this.distEdgeIds.clear();
      this.timeVertexIds.clear(); this.timeEdgeIds.clear();
      const lines = ['查询点附近 100 个地点：'];
      for (let i = 0; i < vertices.length; i++) {
        const v = vertices[i];
        this.queryVertexIds.add(v.id);
        for (const e of this.graph.adjacency.get(v.id) || []) this.queryEdgeIds.add(e.id);
        if (i < 12) lines.push(`${i+1}. ${v.name} (${v.type}) [${v.x.toFixed(1)}, ${v.y.toFixed(1)}]`);
      }
      lines.push(`共查询到 ${vertices.length} 个地点。`);
      document.getElementById('resultArea').value = lines.join('\n');
      this._setStatus('已显示附近 100 个地点及其关联道路');
      this._repaint();
    } catch(ex) { this._showToast(ex.message); }
  }

  _distPath() {
    if (this.startId == null || this.endId == null) { this._showToast('请先在地图上依次选择 A 点和 B 点'); return; }
    try {
      const r = this.pathFinder.findShortestByDist(this.startId, this.endId);
      this.distVertexIds = new Set(r.vertexIds); this.distEdgeIds = new Set(r.edgeIds);
      this.timeVertexIds.clear(); this.timeEdgeIds.clear();
      this.queryVertexIds.clear(); this.queryEdgeIds.clear();
      document.getElementById('resultArea').value = `距离最短路径\n顶点数：${r.vertexIds.length}\n总距离：${r.totalDist.toFixed(2)}\n预计时间：${r.totalTime.toFixed(2)}\n路径：${r.vertexIds.join(',')}`;
      this._setStatus('已计算距离最短路径');
      this._repaint();
    } catch(ex) { this._showToast(ex.message); }
  }

  _timePath() {
    if (this.startId == null || this.endId == null) { this._showToast('请先在地图上依次选择 A 点和 B 点'); return; }
    try {
      this.showTraffic = true;
      const r = this.pathFinder.findShortestByTime(this.startId, this.endId);
      this.timeVertexIds = new Set(r.vertexIds); this.timeEdgeIds = new Set(r.edgeIds);
      this.distVertexIds.clear(); this.distEdgeIds.clear();
      this.queryVertexIds.clear(); this.queryEdgeIds.clear();
      document.getElementById('resultArea').value = `路况最优路径\n顶点数：${r.vertexIds.length}\n总距离：${r.totalDist.toFixed(2)}\n预计时间：${r.totalTime.toFixed(2)}\n路径：${r.vertexIds.join(',')}`;
      this._setStatus('已计算当前路况下的最优路径');
      this._repaint();
    } catch(ex) { this._showToast(ex.message); }
  }

  _simStart() {
    this.showTraffic = true;
    this.traffic.start();
    if (this.simInterval) clearInterval(this.simInterval);
    this.simInterval = setInterval(() => {
      this.traffic.tick();
      this._repaint();
      this._setStatus(`模拟中，活跃车辆数：${this.traffic.vehicles.length}`);
    }, CONFIG.simIntervalMs);
    this._setStatus('车流模拟已启动');
  }

  _simPause() {
    this.traffic.pause();
    if (this.simInterval) { clearInterval(this.simInterval); this.simInterval = null; }
    this._setStatus('车流模拟已暂停');
  }

  _simReset() {
    if (this.simInterval) { clearInterval(this.simInterval); this.simInterval = null; }
    this.traffic.reset();
    this.showTraffic = false;
    this._repaint();
    this._setStatus('车流模拟已重置');
  }

  _setStatus(msg) { document.getElementById('statusBar').textContent = msg; }
  _showToast(msg) {
    const t = document.getElementById('toast');
    t.textContent = msg; t.classList.add('show');
    clearTimeout(t._timeout);
    t._timeout = setTimeout(() => t.classList.remove('show'), 2000);
  }
}

function ptSegDistSq(x1, y1, x2, y2, px, py) {
  const dx = x2 - x1, dy = y2 - y1;
  const len2 = dx*dx + dy*dy;
  if (len2 === 0) return (px-x1)**2 + (py-y1)**2;
  let t = ((px-x1)*dx + (py-y1)*dy) / len2;
  t = Math.max(0, Math.min(1, t));
  const nx = x1 + t*dx, ny = y1 + t*dy;
  return (px-nx)**2 + (py-ny)**2;
}

// ============================================================================
// BOOT
// ============================================================================
document.addEventListener('DOMContentLoaded', () => new App());
