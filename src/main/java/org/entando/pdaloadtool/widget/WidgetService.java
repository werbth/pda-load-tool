package org.entando.pdaloadtool.widget;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class WidgetService {

    public void loadWidgets(LoadWidgetRequest loadWidgetRequest) {
        log.info("Start loading widgets for bundleId: {}", loadWidgetRequest.getBundleId());
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(loadWidgetRequest.getEntandoApi())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + loadWidgetRequest.getAuthToken())
                .build();

        loadWidgetRequest.getWidgets().forEach(widget -> loadWidget(restTemplate, widget, loadWidgetRequest));
        log.info("Finish loading widgets for bundleId: {}", loadWidgetRequest.getBundleId());
    }

    private void loadWidget(RestTemplate restTemplate, WidgetRequest widget, LoadWidgetRequest loadWidgetRequest) {
        String widgetTitle = getWidgetTitle(widget);
        WidgetDto widgetDto = WidgetDto.builder()
                .code(loadWidgetRequest.getBundleId() + "_" + widget.getName().replaceAll("-", "_"))
                .titles(ImmutableMap.of("en", widgetTitle, "it", widgetTitle))
                .group("free")
                .customUi(getCustomUi(widget, loadWidgetRequest))
                .bundleId(loadWidgetRequest.getBundleId())
                .configUi(ImmutableMap.of(
                        "customElement", widget.getName() + "-config",
                        "resources", loadWidgetRequest.getResources().stream()
                                .filter(e -> e.endsWith(".js"))
                                .map(e -> loadWidgetRequest.getBundleId() + "/" + e)
                                .collect(Collectors.toList())
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

    private String getWidgetTitle(WidgetRequest widgetRequest) {
        return "PDA - " + Arrays.stream(widgetRequest.getName().split("-"))
                .map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    private String getCustomUi(WidgetRequest widgetRequest, LoadWidgetRequest loadWidgetRequest) {
        StringBuilder customUi = new StringBuilder();
        customUi.append("<#assign wp=JspTaglibs[\"/aps-core\"]>\n");
        if (widgetRequest.isHasTaskId()) {
            customUi.append("<#if RequestParameters.taskId?exists>\n");
            customUi.append("    <#assign taskId= RequestParameters.taskId>\n");
            customUi.append("<#else>   \n");
            customUi.append("    <#assign taskId= \"\">\n");
            customUi.append("</#if>\n");
        }
        customUi.append("<script crossorigin src=\"https://unpkg.com/react@16/umd/react.development.js\"></script>\n");
        customUi.append("<script crossorigin src=\"https://unpkg.com/react-dom@16/umd/react-dom.development.js\"></script>\n");
        customUi.append(getResources(loadWidgetRequest.getResources(), loadWidgetRequest.getBundleId()));
        customUi.append("<").append(widgetRequest.getName()).append(" service-url=\"")
                .append(loadWidgetRequest.getServiceUrl());
        customUi.append("\" page-code=\"${Request.reqCtx.getExtraParam('currentPage').code}\" frame-id=\"${Request.reqCtx.getExtraParam('currentFrame')}\"");
        customUi.append((widgetRequest.isHasTaskId() ? " id=\"${taskId}\"/>" : "/>"));
        return customUi.toString();
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
