package org.sonarsource.branch.scanner;

import java.util.Collections;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.sonar.api.utils.MessageException;
import org.sonar.scanner.scan.branch.BranchConfiguration;
import org.sonar.scanner.scan.branch.BranchInfo;
import org.sonar.scanner.scan.branch.BranchType;
import org.sonar.scanner.scan.branch.DefaultBranchConfiguration;
import org.sonar.scanner.scan.branch.ProjectBranches;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BranchConfigurationLoaderImplTest {
    private BranchConfigurationLoaderImpl branchConfigurationLoader = new BranchConfigurationLoaderImpl();

    @Test
    public void load_default() {
        BranchConfiguration configuration = branchConfigurationLoader.load(
                ImmutableMap.of("",""),
                () -> ImmutableMap.of("",""),
                new ProjectBranches(Collections.emptyList())
        );

        assertNotNull(configuration);
        assertTrue(configuration instanceof DefaultBranchConfiguration);
    }

    @Test(expected = MessageException.class)
    public void load_no_project_branches() {
        BranchConfiguration configuration = branchConfigurationLoader.load(
                ImmutableMap.of("sonar.branch.name", "test"),
                () -> ImmutableMap.of("",""),
                new ProjectBranches(Collections.emptyList())
        );
    }

    @Test
    public void load_main() {
        BranchConfiguration configuration = branchConfigurationLoader.load(
                ImmutableMap.of("sonar.branch.name", "master"),
                () -> ImmutableMap.of("",""),
                new ProjectBranches(ImmutableList.of(new BranchInfo("master", BranchType.LONG, false, null)))
        );

        assertNotNull(configuration);
        assertEquals("master", configuration.branchName());
        assertEquals(BranchType.LONG, configuration.branchType());
        assertNull(configuration.branchTarget());
        assertEquals("master", configuration.branchBase());
    }

    @Test
    public void load_target() {
        BranchConfiguration configuration = branchConfigurationLoader.load(
                ImmutableMap.of(
                        "sonar.branch.name", "test",
                        "sonar.branch.target", "master"),
                () -> ImmutableMap.of("",""),
                new ProjectBranches(ImmutableList.of(
                        new BranchInfo("test", BranchType.SHORT, false, "master"),
                        new BranchInfo("master", BranchType.LONG, true, null)
                ))
        );

        assertNotNull(configuration);
        assertEquals("test", configuration.branchName());
        assertEquals(BranchType.SHORT, configuration.branchType());
        assertEquals("master", configuration.branchTarget());
        assertEquals("master", configuration.branchBase());
    }

    @Test
    public void load_nested_target() {
        BranchConfiguration configuration = branchConfigurationLoader.load(
                ImmutableMap.of(
                        "sonar.branch.name", "test",
                        "sonar.branch.target", "branch"),
                () -> ImmutableMap.of("",""),
                new ProjectBranches(ImmutableList.of(
                        new BranchInfo("test", BranchType.SHORT, false, "branch"),
                        new BranchInfo("branch", BranchType.SHORT, false, "master"),
                        new BranchInfo("master", BranchType.LONG, true, null)
                ))
        );

        assertNotNull(configuration);
        assertEquals("test", configuration.branchName());
        assertEquals(BranchType.SHORT, configuration.branchType());
        assertEquals("master", configuration.branchTarget());
        assertEquals("branch", configuration.branchBase());
    }

    @Test
    public void load_regex_default() {
        BranchConfiguration configuration = branchConfigurationLoader.load(
                ImmutableMap.of(
                        "sonar.branch.name", "release-123",
                        "sonar.branch.target", "master"),
                () -> ImmutableMap.of("",""),
                new ProjectBranches(ImmutableList.of(
                        new BranchInfo("master", BranchType.LONG, true, null)
                ))
        );

        assertNotNull(configuration);
        assertEquals("release-123", configuration.branchName());
        assertEquals(BranchType.LONG, configuration.branchType());
        assertEquals("master", configuration.branchTarget());
        assertEquals("master", configuration.branchBase());
    }

    @Test
    public void load_regex_custom() {
        BranchConfiguration configuration = branchConfigurationLoader.load(
                ImmutableMap.of(
                        "sonar.branch.name", "test",
                        "sonar.branch.target", "master"),
                () -> ImmutableMap.of("sonar.branch.longLivedBranches.regex","test"),
                new ProjectBranches(ImmutableList.of(
                        new BranchInfo("master", BranchType.LONG, true, null)
                ))
        );

        assertNotNull(configuration);
        assertEquals("test", configuration.branchName());
        assertEquals(BranchType.LONG, configuration.branchType());
        assertEquals("master", configuration.branchTarget());
        assertEquals("master", configuration.branchBase());
    }

    @Test
    public void load_regex_custom_not_matching() {
        BranchConfiguration configuration = branchConfigurationLoader.load(
                ImmutableMap.of(
                        "sonar.branch.name", "test",
                        "sonar.branch.target", "master"),
                () -> ImmutableMap.of("sonar.branch.longLivedBranches.regex","notmatching"),
                new ProjectBranches(ImmutableList.of(
                        new BranchInfo("master", BranchType.LONG, true, null)
                ))
        );

        assertNotNull(configuration);
        assertEquals("test", configuration.branchName());
        assertEquals(BranchType.SHORT, configuration.branchType());
        assertEquals("master", configuration.branchTarget());
        assertEquals("master", configuration.branchBase());
    }
}