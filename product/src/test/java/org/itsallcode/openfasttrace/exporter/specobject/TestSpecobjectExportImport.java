package org.itsallcode.openfasttrace.exporter.specobject;

/*-
 * #%L
 \* OpenFastTrace
 * %%
 * Copyright (C) 2016 - 2017 itsallcode.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.stream.*;

import org.itsallcode.openfasttrace.core.*;
import org.itsallcode.openfasttrace.core.Location;
import org.itsallcode.openfasttrace.importer.SpecificationListBuilder;
import org.itsallcode.openfasttrace.importer.input.InputFile;
import org.itsallcode.openfasttrace.importer.input.StreamInput;
import org.itsallcode.openfasttrace.importer.specobject.SpecobjectImporterFactory;
import org.itsallcode.openfasttrace.testutil.xml.IndentingXMLStreamWriter;
import org.junit.jupiter.api.Test;

import com.github.hamstercommunity.matcher.auto.AutoMatcher;

class TestSpecobjectExportImport
{
    @Test
    void testExportImportSimpleSpecObjectWithMandatoryElements()
            throws IOException, XMLStreamException
    {
        final SpecificationItem item = SpecificationItem.builder() //
                .id(SpecificationItemId.createId("foo", "bar", 1)) //
                .description("the description") //
                .location(Location.create("dummy.xml", 4)) //
                .build();
        assertExportAndImport(item);
    }

    @Test
    void testExportImportSpecObjectWithOptionalElements() throws IOException, XMLStreamException
    {
        final SpecificationItem item = SpecificationItem.builder() //
                .id(SpecificationItemId.createId("req", "me", 2)) //
                .title("My item title") //
                .status(ItemStatus.DRAFT) //
                .description("the description") //
                .rationale("the rationale") //
                .comment("the comment") //
                .addCoveredId("feat", "covered", 1) //
                .addDependOnId("req", "depend-on", 1) //
                .addNeedsArtifactType("impl") //
                .addTag("the tag") //
                .location(Location.create("dummy.xml", 4)) //
                .build();
        assertExportAndImport(item);
    }

    @Test
    void testExportImportTwoSpecObjects() throws IOException, XMLStreamException
    {
        final SpecificationItem itemA = SpecificationItem.builder() //
                .id(SpecificationItemId.createId("foo", "bar", 1)) //
                .status(ItemStatus.PROPOSED) //
                .description("the description") //
                .rationale("the rationale") //
                .comment("the comment") //
                .location(Location.create("dummy.xml", 4)) //
                .build();
        final SpecificationItem itemB = SpecificationItem.builder() //
                .id(SpecificationItemId.createId("baz", "zoo", 2)) //
                .status(ItemStatus.REJECTED) //
                .description("another\ndescription") //
                .rationale("another\nrationale") //
                .comment("another\ncomment") //
                .location(Location.create("dummy.xml", 5)) //
                .build();
        assertExportAndImport(itemA, itemB);
    }

    private void assertExportAndImport(final SpecificationItem... items)
    {
        final String exportedItems = exportToString(items);
        final List<SpecificationItem> importedItems = importItems(exportedItems);
        System.out.println(importedItems);
        System.out.println(asList(items));

        assertThat(importedItems, AutoMatcher.contains(items));
    }

    private List<SpecificationItem> importItems(final String exportedItems)
    {
        final InputFile input = StreamInput.forContent(Paths.get("dummy.xml"), exportedItems);
        final SpecificationListBuilder itemBuilder = SpecificationListBuilder.create();
        new SpecobjectImporterFactory().createImporter(input, itemBuilder).runImport();
        return itemBuilder.build();
    }

    private String exportToString(final SpecificationItem... items)
    {
        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            final XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(stream,
                    StandardCharsets.UTF_8.name());
            new SpecobjectExporter(asList(items).stream(),
                    new IndentingXMLStreamWriter(xmlWriter, " ", Newline.UNIX.toString()),
                    new OutputStreamWriter(stream), Newline.UNIX).runExport();
            return new String(stream.toByteArray(), StandardCharsets.UTF_8);
        }
        catch (IOException | XMLStreamException | FactoryConfigurationError e)
        {
            throw new AssertionError("Error exporting items", e);
        }
    }
}
