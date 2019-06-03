package fboeller

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Files
import java.nio.file.Files.isRegularFile
import java.nio.file.Path
import java.util.stream.Collectors
import java.nio.file.FileSystems



data class Project(val compilationUnits: List<CompilationUnit>)

object ProjectParser {

    val javaMatcher = FileSystems.getDefault().getPathMatcher("glob:**.java")

    fun readDirectory(path: Path): Project =
            Project(javaFiles(path).map { StaticJavaParser.parse(it) })

    fun javaFiles(path: Path): List<Path> =
            Files.walk(path).use { paths ->
                paths
                        .filter { isRegularFile(it) }
                        .filter { javaMatcher.matches(it) }
                        .collect(Collectors.toList())
            }

}