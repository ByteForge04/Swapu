"""
Generate a clean order state machine draw.io diagram matching the Mermaid stateDiagram-v2.
"""
import datetime

OUTPUT = 'd:/SwapU/docs/diagrams/order_state_machine.drawio'

# Escaping helper
def esc(text):
    return text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;').replace('"', '&quot;')

cells = []
next_id = 2
def vid():
    global next_id; cid = str(next_id); next_id += 1; return cid

def vertex(value, x, y, w, h, style, parent='1'):
    cid = vid()
    cells.append(f'<mxCell id="{cid}" value="{esc(value)}" style="{style}" vertex="1" parent="{parent}">\n  <mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/>\n</mxCell>')
    return cid

def edge(value, source, target, style, extra=''):
    cid = vid()
    full_style = style + extra if extra else style
    cells.append(f'<mxCell id="{cid}" value="{esc(value)}" style="{full_style}" edge="1" parent="1" source="{source}" target="{target}">\n  <mxGeometry relative="1" as="geometry"/>\n</mxCell>')
    return cid

# ── Styles ──────────────────────────
ST = 'rounded=1;whiteSpace=wrap;html=1;fontSize=14;fontFamily=Courier New;'
S_START  = ST + 'fillColor=#333333;strokeColor=#333333;fontColor=#ffffff;'  # black circle-like
S_STATE  = ST + 'fillColor=#dae8fc;strokeColor=#6c8ebf;'
S_CANCEL = ST + 'fillColor=#ffe6cc;strokeColor=#d79b00;'
S_DONE   = ST + 'fillColor=#f8cecc;strokeColor=#b85450;'
S_NOTE   = 'shape=note;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;fontSize=11;fontFamily=Courier New;align=left;spacingLeft=8;'
S_INIT   = 'ellipse;whiteSpace=wrap;html=1;fillColor=#333333;strokeColor=#333333;'  # ● start node

E_BASE = 'edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;jettySize=auto;html=1;strokeColor=#555555;fontSize=12;fontFamily=Courier New;'

# ── Layout  ─────────────────────────
# Center column: states at x=400, notes at x=680
# y: 80(start) → 180(pending) → 350(progress) → 520(done / cancelled)

# Start bullet (●)
s_init = vertex('', 505, 65, 30, 30, S_INIT)

# States
s_pending  = vertex('<b>待卖家确认</b>', 430, 140, 180, 55, S_STATE)
s_progress = vertex('<b>进行中</b>',     430, 320, 180, 55, S_STATE)
s_done     = vertex('<b>已完成</b>',     310, 480, 160, 55, S_DONE)
s_cancelled= vertex('<b>已取消</b>',     550, 480, 160, 55, S_CANCEL)

# End bullets (●→)
s_end1 = vertex('', 380, 570, 20, 20, S_INIT)
s_end2 = vertex('', 620, 570, 20, 20, S_INIT)

# Notes (right side)
vertex('<b>status = 0</b><br>payment_status = 0<br><br>分布式锁保护', 680, 135, 160, 70, S_NOTE)
vertex('<b>status = 1</b><br>payment_status = 1',                 680, 315, 160, 50, S_NOTE)
vertex('<b>status = 2</b><br><br>可进行评价',                     520, 490, 140, 55, S_NOTE)
vertex('<b>status = 3</b><br><br>商品释放回在售',                  750, 490, 140, 55, S_NOTE)

# Transitions
edge('买家创建订单<br>商品 → 交易中',      s_init,      s_pending,  E_BASE)
edge('卖家确认接单',      s_pending, s_progress,  E_BASE, 'exitY=0.3;entryY=0.3;')
edge('买家支付成功',      s_pending, s_progress,  E_BASE, 'exitY=0.7;entryY=0.7;')
edge('30分钟超时未支付',   s_pending, s_cancelled, E_BASE, 'exitY=0.3;entryY=0.3;')
edge('买家主动取消',      s_pending, s_cancelled, E_BASE, 'exitY=0.7;entryY=0.7;')
edge('任一方取消订单<br>商品 → 回滚在售',    s_progress, s_cancelled, E_BASE)
edge('买家确认收货<br>商品 → 已售出',        s_progress, s_done,     E_BASE)
edge('', s_done,     s_end1, E_BASE)
edge('', s_cancelled, s_end2, E_BASE)

# ── Render ───────────────────────────
xml = f'''<?xml version="1.0" encoding="UTF-8"?>
<mxfile host="app.diagrams.net" modified="{datetime.datetime.now().isoformat()}" agent="SwapU" version="24.0.0" type="device">
  <diagram name="订单状态机" id="order-fsm">
    <mxGraphModel dx="1422" dy="794" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1000" pageHeight="700" math="0" shadow="0">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        {''.join(cells)}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>'''

with open(OUTPUT, 'w', encoding='utf-8') as f:
    f.write(xml)
print(f'Done: {OUTPUT} ({len(xml)} bytes)')
