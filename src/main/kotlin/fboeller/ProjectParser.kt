package fboeller

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import java.nio.file.Files
import java.nio.file.Files.isRegularFile
import java.nio.file.Path
import java.util.stream.Collectors
import java.nio.file.FileSystems

object ProjectParser {

    val javaMatcher = FileSystems.getDefault().getPathMatcher("glob:**.java")

    fun readDirectory(path: Path): List<CompilationUnit> =
            javaFiles(path).map { StaticJavaParser.parse(it) }

    fun javaFiles(path: Path): List<Path> =
            Files.walk(path).use { paths ->
                paths
                        .filter { isRegularFile(it) }
                        .filter { javaMatcher.matches(it) }
                        .collect(Collectors.toList())
            }

}