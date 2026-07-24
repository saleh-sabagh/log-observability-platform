package rule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RuleTypeTest {

    @Test
    void values_ReturnsAllConstantsInDeclarationOrder() {
        // Arrange
        RuleType[] expected = {
                RuleType.ERROR,
                RuleType.LEVEL_RATE,
                RuleType.TOTAL_RATE
        };

        // Act
        RuleType[] actual = RuleType.values();

        // Assert
        assertArrayEquals(expected, actual);
        assertEquals(3, actual.length);
    }

    @ParameterizedTest
    @EnumSource(RuleType.class)
    void valueOf_ValidName_ReturnsMatchingConstant(RuleType expected) {
        // Arrange
        String constantName = expected.name();

        // Act
        RuleType actual = RuleType.valueOf(constantName);

        // Assert
        assertSame(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"error", "Error", "LEVEL_rate", "TOTAL_RATE ", "UNKNOWN"})
    void valueOf_InvalidName_ThrowsIllegalArgumentException(String invalidName) {
        // Arrange
        // invalidName provided by parameter source

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> RuleType.valueOf(invalidName));
    }

    @Test
    void valueOf_NullName_ThrowsNullPointerException() {
        // Arrange
        String nullName = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> RuleType.valueOf(nullName));
    }

    @Test
    void name_ReturnsEnumConstantName() {
        // Arrange
        RuleType ruleType = RuleType.LEVEL_RATE;

        // Act
        String name = ruleType.name();

        // Assert
        assertEquals("LEVEL_RATE", name);
    }

    @Test
    void ordinal_ReturnsStableIndexBasedOnDeclarationOrder() {
        // Arrange
        // constants declared as ERROR, LEVEL_RATE, TOTAL_RATE

        // Act & Assert
        assertEquals(0, RuleType.ERROR.ordinal());
        assertEquals(1, RuleType.LEVEL_RATE.ordinal());
        assertEquals(2, RuleType.TOTAL_RATE.ordinal());
    }

    @Test
    void toString_ReturnsConstantName() {
        // Arrange
        RuleType ruleType = RuleType.TOTAL_RATE;

        // Act
        String stringValue = ruleType.toString();

        // Assert
        assertEquals("TOTAL_RATE", stringValue);
    }

    @Test
    void enumSet_ContainsExactlyAllDeclaredConstants() {
        // Arrange
        Set<RuleType> expected = Set.of(
                RuleType.ERROR,
                RuleType.LEVEL_RATE,
                RuleType.TOTAL_RATE
        );

        // Act
        Set<RuleType> actual = Arrays.stream(RuleType.values()).collect(Collectors.toSet());

        // Assert
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(RuleType.class)
    void equals_SameConstant_IsReflexive(RuleType ruleType) {
        // Arrange
        // ruleType provided by parameter source

        // Act & Assert
        assertSame(ruleType, ruleType);
        assertEquals(ruleType, ruleType);
    }

    @Test
    void equals_DifferentConstants_AreNotEqual() {
        // Arrange
        RuleType errorRule = RuleType.ERROR;
        RuleType levelRateRule = RuleType.LEVEL_RATE;

        // Act & Assert
        assertNotEquals(errorRule, levelRateRule);
    }

    @Test
    void switchOnRuleType_NullRuleType_ThrowsNullPointerException() {
        // Arrange
        RuleType ruleType = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> evaluateRuleType(ruleType));
    }

    @ParameterizedTest
    @EnumSource(RuleType.class)
    void switchOnRuleType_AllConstants_AreHandledWithoutException(RuleType ruleType) {
        // Arrange
        // ruleType provided by parameter source

        // Act
        String handledType = evaluateRuleType(ruleType);

        // Assert
        assertTrue(
                handledType.equals("ERROR")
                        || handledType.equals("LEVEL_RATE")
                        || handledType.equals("TOTAL_RATE")
        );
        assertEquals(ruleType.name(), handledType);
    }

    private String evaluateRuleType(RuleType ruleType) {
        return switch (ruleType) {
            case ERROR -> "ERROR";
            case LEVEL_RATE -> "LEVEL_RATE";
            case TOTAL_RATE -> "TOTAL_RATE";
        };
    }
}
