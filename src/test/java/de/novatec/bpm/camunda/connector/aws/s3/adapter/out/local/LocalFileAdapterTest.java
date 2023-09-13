package de.novatec.bpm.camunda.connector.aws.s3.adapter.out.local;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
class LocalFileAdapterTest {

    private static Path baseDir;
    private LocalFileAdapter adapter;

    @BeforeAll
    static void generateTempDir() throws IOException {
        baseDir = Files.createTempDirectory("unit-test-");
    }

    @AfterAll
    static void deleteTempDir() throws IOException {
        for (Path path : baseDir) {
            Files.deleteIfExists(path);
        }
    }

    @BeforeEach
    void setUp() {
        adapter = new LocalFileAdapter(baseDir);
    }

    @Test
    @SneakyThrows(NoSuchFileException.class)
    void file_is_written_and_loaded() throws IOException {
        adapter.saveFile("foo".getBytes(StandardCharsets.UTF_8), "foo.txt");
        byte[] bytes = adapter.loadFile("foo.txt");
        assertThat(new String(bytes)).isEqualTo("foo");
        adapter.loadFile("foo.txt");
    }

    @Test
    void parent_dirs_are_created() throws IOException {
        Path resolve = baseDir.resolve(Path.of("1", "2", "3", "4"));
        assertThat(Files.exists(resolve)).isFalse();
        adapter.saveFile("foo".getBytes(StandardCharsets.UTF_8), "1/2/3/4/foo.txt");
        assertThat(Files.exists(resolve)).isTrue();
    }

    @Test
    void loading_missing_file_throws_exception() {
        assertThatThrownBy(() -> adapter.loadFile("unknown.txt"))
                .isExactlyInstanceOf(NoSuchFileException.class)
                .hasMessageContaining("unknown.txt");
    }

    @Test
    void deleting_missing_file_throws_no_exception() {
        assertThatNoException()
                .isThrownBy(() -> adapter.deleteFile("anyfile.txt"));
    }
}