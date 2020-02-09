package org.entando.pdaloadtool.widget;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WidgetDto {

    private String code;
    private Map<String, String> titles;
    private String group;
    private String customUi;
    private String bundleId;
    private Map<String, Object> configUi;
}
