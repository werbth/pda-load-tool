package org.entando.pdaloadtool.widget;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadWidgetRequest {

    private String entandoApi;
    private String authToken;
    private List<WidgetRequest> widgets;
    private String serviceUrl;
    private String bundleId;
    private List<String> resources;
}
