package org.entando.pdaloadtool.widget;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.RestTemplate;

@Service
public class WidgetService {

    public void loadWidgets(LoadWidgetRequest loadWidgetRequest) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(loadWidgetRequest.getEntandoApi())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + loadWidgetRequest.getAuthToken())
                .build();

        loadWidgetRequest.getWidgets().forEach(widget -> loadWidget(restTemplate, widget, loadWidgetRequest));
    }

    private void loadWidget(RestTemplate restTemplate, String widget, LoadWidgetRequest loadWidgetRequest) {
        String widgetTitle = getWidgetTitle(widget);
        WidgetDto widgetDto = WidgetDto.builder()
                .code(loadWidgetRequest.getBundleId() + "_" + widget.replaceAll("-", "_"))
                .titles(ImmutableMap.of("en", widgetTitle, "it", widgetTitle))
                .group("free")
                .customUi(getCustomUi(widget, loadWidgetRequest))
                .bundleId(loadWidgetRequest.getBundleId())
                .configUi(ImmutableMap.of(
                        "customElement", widget + "-config",
                        "resources", loadWidgetRequest.getResources().stream()
                                .filter(e -> e.endsWith(".js")).collect(Collectors.toList())
                ))
                .build();

        try {
            restTemplate.exchange("/api/widgets/" + widgetDto.getCode(), HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            restTemplate.put("/api/widgets/" + widgetDto.getCode(), widgetDto);
        } catch (NotFound e) {
            restTemplate.postForEntity("/api/widgets", widgetDto, WidgetDto.class);
        }
    }

    private String getWidgetTitle(String name) {
        return "PDA - " + Arrays.stream(name.split("-"))
                .map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    private String getCustomUi(String name, LoadWidgetRequest loadWidgetRequest) {
        return "<#assign wp=JspTaglibs[\"/aps-core\"]>\n"
                + "<script crossorigin src=\"https://unpkg.com/react@16/umd/react.development.js\"></script>\n"
                + "<script crossorigin src=\"https://unpkg.com/react-dom@16/umd/react-dom.development.js\"></script>\n"
                + getResources(loadWidgetRequest.getResources(), loadWidgetRequest.getBundleId())
                + "<" + name + " service-url=\"" + loadWidgetRequest.getServiceUrl()
                + "\" page-code=\"${Request.reqCtx.getExtraParam('currentPage').code}\" frame-id=\"${Request.reqCtx.getExtraParam('currentFrame')}\"/>";
    }

    private String getResources(List<String> resources, String bundleId) {
        return resources.stream().map(resourceName -> {
            if (resourceName.endsWith(".css")) {
                return "<link href=\"<@wp.resourceURL />" + bundleId + "/" + resourceName
                        + "\" rel=\"stylesheet\">\n";
            } else if (resourceName.endsWith(".js")) {
                return "<script src=\"<@wp.resourceURL />" + bundleId + "/" + resourceName + "\"></script>\n";
            }
            return "";
        }).collect(Collectors.joining());
    }
}
