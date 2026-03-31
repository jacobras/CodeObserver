package nl.jacobras.codeobserver.dashboard.modulegraph

internal class MermaidContainerBuilder {
    fun buildMermaidWebPage(mermaidGraph: String): String {
        return """<!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            <title>Dependency Graph</title>
            <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/d3@7"></script>
            <style>
                html, body {
                    margin: 0;
                    height: 100%;
                    overflow: hidden;
                    background: white;
                    font-family: sans-serif;
                }
                #viewport {
                    width: 100%;
                    height: 100%;
                    cursor: grab;
                }
                #viewport:active {
                    cursor: grabbing;
                }
                #error {
                    position: absolute;
                    top: 10px;
                    left: 10px;
                    color: red;
                    font-family: monospace;
                    white-space: pre;
                }
            </style>
        </head>
        <body>
        <div id="viewport"></div>
        <div id="error" hidden></div>
        <div id="graph-data" hidden>$mermaidGraph</div>
        <script>
            mermaid.initialize({ startOnLoad: false });
        
            async function loadGraph() {
                const viewport = document.getElementById("viewport");
                const errorDiv = document.getElementById("error");
                const graphDataElement = document.getElementById("graph-data");
                const graphDefinition = graphDataElement ? graphDataElement.textContent : "";
                try {
                    const {svg} = await mermaid.render("graphSvg", graphDefinition);
                    viewport.innerHTML = svg;
                    const svgElement = viewport.querySelector("svg");
                    svgElement.style.width = "100%";
                    svgElement.style.height = "100%";
                    svgElement.style.display = "block";
                    svgElement.removeAttribute("width");
                    svgElement.removeAttribute("height");
                    const g = document.createElementNS("http://www.w3.org/2000/svg", "g");
                    while (svgElement.firstChild)
                        g.appendChild(svgElement.firstChild);
                    svgElement.appendChild(g);
                    const zoom = d3.zoom()
                        .scaleExtent([0.2, 10])
                        .on("zoom", (event) => {
                            g.setAttribute("transform", event.transform);
                        });
                    d3.select(svgElement).call(zoom);
                } catch (e) {
                    errorDiv.hidden = false;
                    errorDiv.textContent = "Failed to load graph:\n\n" + e;
                }
            }
        
            loadGraph();
        </script>
        </body>
        </html>
        """.trimIndent()
    }
}