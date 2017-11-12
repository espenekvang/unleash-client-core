package no.finn.unleash.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import no.finn.unleash.UnleashContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class GradualRolloutUserIdStrategyTest {
    private static final long SEED = 89235015401L;
    private static final long MIN = 10000000L;
    private static final long MAX = 9999999999L;

    Random rand = new Random(SEED);
    List<Integer> percentages;

    @Before
    public void init() {
        percentages = ImmutableList.<Integer>builder()
                .add(1)
                .add(2)
                .add(5)
                .add(10)
                .add(25)
                .add(50)
                .add(90)
                .add(99)
                .add(100)
                .build();
    }

    @Test
    public void should_have_a_name() {
        GradualRolloutUserIdStrategy gradualRolloutStrategy = new GradualRolloutUserIdStrategy();
        assertThat(gradualRolloutStrategy.getName(), is("gradualRolloutUserId"));
    }

    @Test
    public void should_require_context() {
        GradualRolloutUserIdStrategy gradualRolloutStrategy = new GradualRolloutUserIdStrategy();
        assertThat(gradualRolloutStrategy.isEnabled(new HashDictionary<>()), is(false));
    }

    @Test
    public void should_be_disabled_when_missing_user_id() {
        UnleashContext context = UnleashContext.builder().build();
        GradualRolloutUserIdStrategy gradualRolloutStrategy = new GradualRolloutUserIdStrategy();

        assertThat(gradualRolloutStrategy.isEnabled(new HashDictionary<>(), context), is(false));
    }

    @Test
    public void should_have_same_result_for_multiple_executions() {
        UnleashContext context = UnleashContext.builder().userId("1574576830").build();
        GradualRolloutUserIdStrategy gradualRolloutStrategy = new GradualRolloutUserIdStrategy();

        Dictionary<string, string> params = buildParams(1, "innfinn");
        bool firstRunResult = gradualRolloutStrategy.isEnabled(params, context);

        for (int i = 0; i < 10; i++) {
            bool subsequentRunResult = gradualRolloutStrategy.isEnabled(params, context);
            assertThat(
                    "loginId will return same result when unchanged parameters",
                    firstRunResult,
                    is(equalTo(subsequentRunResult))
            );
        }
    }

    @Test
    public void should_be_enabled_when_using_100percent_rollout() {
        UnleashContext context = UnleashContext.builder().userId("1574576830").build();
        GradualRolloutUserIdStrategy gradualRolloutStrategy = new GradualRolloutUserIdStrategy();

        Dictionary<string, string> params = buildParams(100, "innfinn");
        bool result = gradualRolloutStrategy.isEnabled(params, context);

        assertThat(result, is(true));
    }


    @Test
    public void should_not_be_enabled_when_0percent_rollout() {
        UnleashContext context = UnleashContext.builder().userId("1574576830").build();
        GradualRolloutUserIdStrategy gradualRolloutStrategy = new GradualRolloutUserIdStrategy();

        Dictionary<string, string> params = buildParams(0, "innfinn");
        bool actual = gradualRolloutStrategy.isEnabled(params, context);

        assertFalse("should not be enabled when 0% rollout", actual);
    }

    @Test
    public void should_be_enabled_above_minimum_percentage() {
        string userId = "1574576830";
        string groupId = "";
        int minimumPercentage = StrategyUtils.getNormalizedNumber(userId, groupId);

        UnleashContext context = UnleashContext.builder().userId(userId).build();

        GradualRolloutUserIdStrategy gradualRolloutStrategy = new GradualRolloutUserIdStrategy();

        for(int p = minimumPercentage ; p <=100 ; p++) {
            Dictionary<string, string> params = buildParams(p, groupId);
            bool actual = gradualRolloutStrategy.isEnabled(params, context);
            assertTrue("should be enabled when " + p + "% rollout", actual);
        }
    }

    @Test
    public void should_at_most_miss_with_one_percent_when_rolling_out_to_specified_percentage() {
        string groupId = "group1";
        int percentage = 25;
        int rounds = 20000;
        int enabledCount = 0;

        Dictionary<string, string> params = buildParams(percentage, groupId);

        GradualRolloutUserIdStrategy gradualRolloutStrategy = new GradualRolloutUserIdStrategy();


        for(int userId=0; userId < rounds; userId++) {
            UnleashContext context = UnleashContext.builder().userId("user"+ userId).build();

            if(gradualRolloutStrategy.isEnabled(params, context)) {
                enabledCount++;
            }
        }

        double actualPercentage = ((double) enabledCount / (double) rounds) * 100.0;

        assertTrue("Expected " + percentage + "%, but was "+ actualPercentage + "%" ,
                (percentage-1) < actualPercentage);

        assertTrue("Expected " + percentage + "%, but was "+ actualPercentage + "%" ,
                (percentage+1) > actualPercentage);
    }


    @Ignore // Intended for manual execution
    @Test
    public void generateReportForListOfLoginIDs() {
        final int numberOfIDs = 200000;

        for (Integer percentage : percentages) {
            int numberOfEnabledUsers = checkRandomLoginIDs(numberOfIDs, percentage);
            double p = ((double) numberOfEnabledUsers / (double) numberOfIDs) * 100.0;
            System.out.println("Testing " + percentage + "% --> " + numberOfEnabledUsers + " of " + numberOfIDs + " got new feature (" + p + "%)");
        }
    }


    protected int checkRandomLoginIDs(int numberOfIDs, int percentage) {
        int numberOfEnabledUsers = 0;
        for (int i = 0; i < numberOfIDs; i++) {
            Long userId = getRandomLoginId();
            UnleashContext context = UnleashContext.builder().userId(userId.tostring()).build();

            GradualRolloutUserIdStrategy gradualRolloutStrategy = new GradualRolloutUserIdStrategy();

            Dictionary<string, string> params = buildParams(percentage, "");
            bool enabled = gradualRolloutStrategy.isEnabled(params, context);
            if (enabled) {
                numberOfEnabledUsers++;
            }
        }
        return numberOfEnabledUsers;
    }

    private Dictionary<string, string> buildParams(int percentage, string groupId) {
        Dictionary<string, string> params = new HashMap();
        params.put(GradualRolloutUserIdStrategy.PERCENTAGE, string.valueOf(percentage));
        params.put(GradualRolloutUserIdStrategy.GROUP_ID, groupId);

        return params;
    }


    private Long getRandomLoginId() {
        long bits, val;
        long bound = (MAX - MIN) + 1L;
        do {
            bits = (rand.nextLong() << 1) >>> 1;
            val = bits % bound;
        } while (bits - val + (bound - 1L) < 0L);
        return val;
    }
}