package org.entando.pdaloadtool.widget;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("widgets")
@RequiredArgsConstructor
public class WidgetController {

    private final WidgetService widgetService;

    @PostMapping("/load")
    public void loadPdaWidgets(@RequestBody LoadWidgetRequest loadWidgetRequest) {
        widgetService.loadWidgets(loadWidgetRequest);
    }
}
