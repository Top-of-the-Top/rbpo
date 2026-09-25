---
name: designing-diagrams
description: Use when drawing any diagram in this repo — DFD, architecture, sequence, flowchart, ER — in mermaid or another text format, or when an existing diagram is hard to read, has crossing or looping arrows, or carries more than about ten arrows.
---

# Designing Diagrams

## Overview

A diagram answers one question and is read in under a minute. Detail lives in a table under the diagram, not on the arrows.

## Recipe

1. **One diagram — one scenario.** At most ~8 nodes and ~10 arrows. More than that → an overview diagram (grouped nodes, short labels without IDs) plus fragment diagrams, one per scenario.
2. **Arrow label = ID + 2–5 words of concrete data**: `F11: taskId, статус, причина`. Never "данные", "запрос".
3. **Table under every diagram**, one row per ID: `ID | откуда → куда | данные | граница/протокол`. Everything the label left out goes here.
4. **An element repeated across fragments keeps its ID**; its table row appears once, in the first fragment.
5. **Reading order**: declare nodes left to right — sources (users, callers) → processes → stores/external systems.
6. **Layout for mermaid flowcharts:**
   ```
   %%{init: {"flowchart": {"defaultRenderer": "elk"}}}%%
   flowchart LR
   ```
   Mark zones/boundaries with a `classDef` on nodes (e.g. dashed orange border + "за TB1" in the label) and one legend line above the diagrams — not with `subgraph`: subgraphs make ELK and dagre route arrows around the whole picture.
7. **Levels are different diagrams, not more arrows.** Logical = business actions and data, no technology. Physical = components, protocols, endpoints (not ports), stores by technology, with a table mapping physical flows to logical IDs.
8. **Render and look before handing over:**
   ```bash
   npx -y -p @mermaid-js/mermaid-cli mmdc -q -s 1.5 -i d.mmd -o d_v2.png
   ```
   Open the PNG. Use a new output filename each render — the image viewer caches by path.

## Common Mistakes

| Symptom | Fix |
| --- | --- |
| 30–40 arrows on one diagram | Overview + fragments by scenario |
| Arrows loop around the canvas | Remove `subgraph` zones, use ELK, declare nodes in reading order |
| Long sentences on arrows | Move detail to the table, keep ID + few words |
| Same flow numbered differently in two fragments | Reuse the ID |
| Protocol/port on a logical diagram | Move to the physical diagram |
| Diagram "checked" by syntax only | Render to PNG and look at it |
