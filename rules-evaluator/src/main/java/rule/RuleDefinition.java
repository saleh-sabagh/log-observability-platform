package rule;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RuleDefinition {

    private String ruleName;
    private RuleType ruleType;
    private boolean enabled;
    private String component;       // اگر null باشد، شامل تمام مؤلفه‌ها می‌شود
    private String logLevel;        // مثل ERROR یا WARN (برای نوع ERROR و LEVEL_RATE)
    private int threshold;          // حد آستانه برای تعداد یا نرخ
    private long timeWindowSeconds; // بازه زمانی به ثانیه (برای قوانین مبتنی بر نرخ)
    private int maxLastMessages;    // تعداد پیام‌های اخیر لاگ جهت درج در توضیحات هشدار
}