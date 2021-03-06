package se.jensim.gradle.plugin.kt2ts

import com.nhaarman.mockitokotlin2.mock
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Test
import se.jensim.gradle.plugin.kt2ts.internal.Kt2TsException
import java.io.File
import kotlin.test.assertFailsWith

class Kt2TsPluginExtensionTest {

    private val project = ProjectBuilder.builder().build().also {
        it.extensions.create("kt2ts", Kt2TsPluginExtension::class.java)
    }


    @After
    fun tearDown() {
        project.kt2ts.outputs.clear()
        project.kt2ts.classFilesSources.classesDirs = null
        project.kt2ts.classFilesSources.compileTasks = null
    }

    @Test
    fun `kt2ts extension func`() {
        // when
        project.kt2ts {
            classFilesSources {
                this.classesDirs = mock()
            }
            output {
                this.annotations = listOf("foo.bar")
                this.outputFile = File("./ts.d.ts")
            }
        }

        // then
        val annotation = project.extensions.findByType(Kt2TsPluginExtension::class.java)?.outputs?.firstOrNull()
            ?.annotations?.firstOrNull()
        assertThat(annotation, equalTo("foo.bar"))
    }

    @Test
    fun `Kt2TsPluginExtension is mutable`() {
        val extension = Kt2TsPluginExtension()
        val settings: List<() -> Any?> =
            listOf(
                { extension.outputs.firstOrNull()?.outputFile },
                { extension.outputs.firstOrNull()?.annotations },
                { extension.classFilesSources.compileTasks },
                { extension.classFilesSources.classesDirs }
            )

        assertThat(settings.mapNotNull { it.invoke() }, hasSize(0))

        extension.output {
            outputFile = File("./")
            annotations = listOf("foo")
        }
        extension.classFilesSources {
            classesDirs = mock()
            compileTasks = mock()
        }

        assertThat(settings.mapNotNull { it.invoke() }, hasSize(4))
    }

    @Test
    fun `only annotation is required`() {
        assertThat(project.kt2ts.annotation, nullValue())

        project.kt2ts.annotation = "foo.bar"

        assertThat(project.kt2ts.outputs.firstOrNull()?.outputFile, notNullValue())
        assertFailsWith<Kt2TsException> { project.kt2ts.classDirFiles }

        val fileCollection = project.files(File("./cd"))
        project.kt2ts.classFilesSources.classesDirs = fileCollection
        val classDirFiles = project.kt2ts.classDirFiles
        assertThat(classDirFiles, hasSize(1))
        assertThat(classDirFiles.first(), equalTo(fileCollection.singleFile))

    }

    @Test
    fun `shorthand annotations`() {
        assertThat(project.kt2ts.annotations, hasSize(0))

        project.kt2ts.annotations = listOf("foo.bar")

        assertThat(project.kt2ts.annotations, hasSize(1))
    }

    @Test
    fun `default file on first output`() {
        assertThat(project.kt2ts.outputs, hasSize(0))

        project.kt2ts.output {
            annotations = listOf("foo.bar")
        }

        assertThat(project.kt2ts.outputs, hasSize(1))
        assertThat(project.kt2ts.outputs.firstOrNull()?.outputFile, notNullValue())
    }

    @Test
    fun `second output needs a outputFile`() {
        project.kt2ts.output {
            annotations = listOf("foo.bar")
        }
        assertFailsWith<IllegalArgumentException> {
            project.kt2ts.output {
                annotations = listOf("bar.foo")
            }
        }
    }

    @Test
    fun `annotations cant be empty`() {
        assertFailsWith<IllegalArgumentException> {
            project.kt2ts.annotations = emptyList()
        }
    }

    @Test
    fun `outputFile must be unique`() {
        project.kt2ts.output {
            annotations = listOf("foo.bar")
            outputFile = project.file("${project.buildDir}/ts/out.d.ts")
        }
        assertFailsWith<IllegalArgumentException> {
            project.kt2ts.output {
                annotations = listOf("bar.foo")
                outputFile = project.file("${project.buildDir}/ts/out.d.ts")
            }
        }
    }
}
