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
    private String component;
    private String logLevel;
    private int threshold;
    private long timeWindowSeconds;
    private int maxLastMessages;
}