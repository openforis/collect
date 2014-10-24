package org.openforis.collect.js;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.api.event.AttributeValuesChangedEvent;
import org.openforis.collect.api.event.EntityAddedEvent;
import org.openforis.collect.api.event.Event;
import org.openforis.collect.api.schema.AttributeDef;
import org.openforis.collect.api.schema.EntityDef;
import org.openforis.collect.api.schema.EntityListDef;
import org.openforis.collect.api.schema.ValueType;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Value;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class JsonServlet extends HttpServlet {
    private final Map<String, RequestHandler> postHandlers;
    private final Map<String, RequestHandler> getHandlers;

    private final EntityDef dummySchema = dummySchema();
    private final List<? extends Event> recordEvents = dummyRecordEvents();


    public JsonServlet() {
        postHandlers = new HashMap<String, RequestHandler>() {{

            put("/update-attribute", new RequestHandler() {
                void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                    // TODO: Fire off a command to collect-core
                    String text = IOUtils.toString(req.getInputStream(), "UTF-8");
                    System.out.println("Updated attribute: " + text);
                }
            });

        }};

        getHandlers = new HashMap<String, RequestHandler>() {{

            put("/schema", new RequestHandler() {
                void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                    String schemaJson = new SchemaJsonSerializer().serialize(dummySchema);
                    resp.getWriter().print(schemaJson);
                }
            });

            put("/record", new RequestHandler() {
                void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                    String recordJson = new EventJsonSerializer().serialize(recordEvents);
                    resp.getWriter().print(recordJson);
                }
            });

        }};
    }


    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, postHandlers);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, getHandlers);
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, Map<String, RequestHandler> handlers) throws IOException, ServletException {RequestHandler handler = handlers.get(req.getPathInfo());
        if (handler == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "JSON resource not found.");
            return;
        }
        resp.setContentType("application/json");
        handler.handle(req, resp);
    }

    private abstract class RequestHandler {
        abstract void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
    }


    private List<? extends Event> dummyRecordEvents() {
        return asList(
                new EntityAddedEvent(null, null, "1"),
                new AttributeValuesChangedEvent(null, new HashMap<String, Value>() {{
                    put("attribute id", new TextValue("Attribute text value"));
                }})
        );
    }

    private EntityDef dummySchema() {
        return new EntityDef("plot", "Plot", asList(
                new AttributeDef("plot_number", "Plot Number", ValueType.Number),
                new EntityListDef("trees", "Trees",
                        new EntityDef("tree", "Tree", asList(
                                new AttributeDef("tree_number", "Tree Number", ValueType.Number)
                        ))
                ))
        );
    }
}
