package integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zkstrata.compiler.Arguments;
import zkstrata.compiler.Compiler;
import zkstrata.utils.ArgumentsBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class UserStoriesTest {
    private static final String DATA_PATH = "src/test/resources/userstories/";

    private ArgumentsBuilder argumentsBuilder;

    @BeforeEach
    void setup() {
        this.argumentsBuilder = new ArgumentsBuilder(DATA_PATH, DATA_PATH, DATA_PATH, UserStoriesTest.class);
    }

    @Test
    void User_Story_S1_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = this.argumentsBuilder
                    .withStatement("user_story_s1")
                    .withWitness("myLicense", "user_story_s1.witness")
                    .withSchema("user_story_s1", "user_story_s1")
                    .withInstance("myLicense", "user_story_s1.metadata")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void User_Story_S2_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = this.argumentsBuilder
                    .withStatement("user_story_s2")
                    .withWitness("myPassport", "user_story_s2.witness")
                    .withSchema("user_story_s2", "user_story_s2")
                    .withInstance("myPassport", "user_story_s2.metadata")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void User_Story_S3_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = this.argumentsBuilder
                    .withStatement("user_story_s3")
                    .withWitness("myRecord", "user_story_s3_record.witness")
                    .withWitness("myPrescription", "user_story_s3_prescription.witness")
                    .withSchema("user_story_s3_record", "user_story_s3_record")
                    .withSchema("user_story_s3_prescription", "user_story_s3_prescription")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void User_Story_S4_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = this.argumentsBuilder
                    .withStatement("user_story_s4")
                    .withWitness("myRecord", "user_story_s4_record.witness")
                    .withWitness("myICV", "user_story_s4_icv.witness")
                    .withSchema("user_story_s4_record", "user_story_s4_record")
                    .withSchema("user_story_s4_icv", "user_story_s4_icv")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void User_Story_S5_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = this.argumentsBuilder
                    .withStatement("user_story_s5")
                    .withWitness("myReport", "user_story_s5.witness")
                    .withSchema("user_story_s5", "user_story_s5")
                    .withInstance("myReport", "user_story_s5.metadata")
                    .build();
            new Compiler(args).compile();
        });
    }

    @Test
    void User_Story_S6_Should_Succeed() {
        assertDoesNotThrow(() -> {
            Arguments args = this.argumentsBuilder
                    .withStatement("user_story_s6")
                    .withWitness("myStatement", "user_story_s6.witness")
                    .withSchema("user_story_s6", "user_story_s6")
                    .withInstance("myStatement", "user_story_s6.metadata")
                    .build();
            new Compiler(args).compile();
        });
    }
}
