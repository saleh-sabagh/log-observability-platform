package alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    private Long id;                    // کلید اصلی در دیتابیس (شناسه یکتا)
    private String ruleName;            // اسم قاعده
    private String component;           // اسم مؤلفه‌ای که هشدار براش تولید شده
    private String description;         // توضیح شامل نرخ، متن لاگ‌ها و...
    private LocalDateTime createdAt;    // تاریخ و زمان دقیق تولید هشدار
}