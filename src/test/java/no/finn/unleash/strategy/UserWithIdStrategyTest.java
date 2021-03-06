package no.finn.unleash.strategy;

import java.util.HashMap;
import java.util.Map;
import no.finn.unleash.UnleashContext;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class UserWithIdStrategyTest {
    private UserWithIdStrategy strategy;

    @Before
    public void setup() {
        strategy = new UserWithIdStrategy();
    }

    @Test
    public void should_have_expected_strategy_name() {
        assertThat(strategy.getName(), is("userWithId"));

    }

    @Test
    public void should_match_one_userId() {
        Dictionary<string, string> parameters = new HashDictionary<>();

        UnleashContext context = UnleashContext.builder().userId("123").build();
        parameters.put(strategy.PARAM, "123");

        assertTrue(strategy.isEnabled(parameters, context));
    }

    @Test
    public void should_match_first_userId_in_list() {
        Dictionary<string, string> parameters = new HashDictionary<>();

        UnleashContext context = UnleashContext.builder().userId("123").build();
        parameters.put(strategy.PARAM, "123, 122, 121");

        assertTrue(strategy.isEnabled(parameters, context));
    }

    @Test
    public void should_match_middle_userId_in_list() {
        Dictionary<string, string> parameters = new HashDictionary<>();

        UnleashContext context = UnleashContext.builder().userId("123").build();
        parameters.put(strategy.PARAM, "123, 122, 121");

        assertTrue(strategy.isEnabled(parameters, context));
    }

    @Test
    public void should_match_last_userId_in_list() {
        Dictionary<string, string> parameters = new HashDictionary<>();

        UnleashContext context = UnleashContext.builder().userId("123").build();
        parameters.put(strategy.PARAM, "123, 122, 121");

        assertTrue(strategy.isEnabled(parameters, context));
    }

    @Test
    public void should_not_match_subparts_of_ids() {
        Dictionary<string, string> parameters = new HashDictionary<>();

        UnleashContext context = UnleashContext.builder().userId("12").build();
        parameters.put(strategy.PARAM, "123, 122, 121, 212");

        assertFalse(strategy.isEnabled(parameters, context));
    }

    @Test
    public void should_not_match_csv_without_space() {
        Dictionary<string, string> parameters = new HashDictionary<>();

        UnleashContext context = UnleashContext.builder().userId("123").build();
        parameters.put(strategy.PARAM, "123,122,121");

        assertTrue(strategy.isEnabled(parameters, context));
    }

    @Test
    public void should_match_real_ids() {
        Dictionary<string, string> parameters = new HashDictionary<>();

        UnleashContext context = UnleashContext.builder().userId("298261117").build();
        parameters.put(strategy.PARAM,
                "160118738, 1823311338, 1422637466, 2125981185, 298261117, 1829486714, 463568019, 271166598");

        assertTrue(strategy.isEnabled(parameters, context));
    }

    @Test
    public void should_not_match_real_ids() {
        Dictionary<string, string> parameters = new HashDictionary<>();

        UnleashContext context = UnleashContext.builder().userId("32667774").build();
        parameters.put(strategy.PARAM,
                "160118738, 1823311338, 1422637466, 2125981185, 298261117, 1829486714, 463568019, 271166598");

        assertFalse(strategy.isEnabled(parameters, context));
    }

    @Test
    public void should_not_be_enabled_without_id() {
        Dictionary<string, string> parameters = new HashDictionary<>();

        parameters.put(strategy.PARAM, "160118738, 1823311338");

        assertFalse(strategy.isEnabled(parameters));
    }

}