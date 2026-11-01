package com.maetschl.kstory.libgdx;

import com.badlogic.gdx.utils.JsonReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KStoryRuntimeTest {
    @Test
    void configNormalizesPortableAssetRoot() {
        KStoryConfig config = KStoryConfig.builder().assetRoot("content").sourceLocale("en").build();
        assertEquals("content/", config.assetRoot());
        assertEquals("en", config.sourceLocale());
    }

    @Test
    void actionPreservesTypedParametersAndCallback() {
        KStoryAction action = KStoryAction.fromJson(
                "action",
                new JsonReader().parse("{callback_id:\"repair\",item_id:\"HAMMER\",amount:2}")
        );
        assertEquals("action", action.type());
        assertEquals("repair", action.callbackId());
        assertEquals("HAMMER", action.parameter("item_id"));
        assertEquals("2", action.parameter("amount"));
    }

    @Test
    void defaultAdapterDoesNotPretendGameplayIsSupported() {
        KStoryGameAdapter adapter = new KStoryGameAdapter() {};
        KStoryAction action = KStoryAction.fromJson("camera_shake_start", null);
        assertFalse(adapter.execute(action).supported);
        assertTrue(adapter.evaluate(KStoryCondition.fromJson(null)));
    }

    @Test
    void localeMetadataUsesSourceThenDeclaredTargetsWithoutDuplicates() {
        KStoryLocalization.LocaleMetadata metadata = KStoryLocalization.metadataFromJson(
                new JsonReader().parse("{source_locale:\"en\",target_locales:[\"es\",\"fr\",\"ES\",\"\"]}"),
                "fallback"
        );

        List<KStoryLocale> locales = metadata.locales;
        assertEquals("en", metadata.sourceLocale);
        assertEquals(3, locales.size());
        assertEquals("en", locales.get(0).code());
        assertTrue(locales.get(0).isSource());
        assertFalse(locales.get(0).isTarget());
        assertEquals("es", locales.get(1).code());
        assertEquals("Español", locales.get(1).displayName());
        assertTrue(locales.get(1).isTarget());
        assertEquals("fr", locales.get(2).code());
    }

    @Test
    void missingMetadataFallsBackToConfiguredSource() {
        KStoryLocalization.LocaleMetadata metadata = KStoryLocalization.metadataFromJson(null, "en");

        assertEquals("en", metadata.sourceLocale);
        assertEquals(1, metadata.locales.size());
        assertEquals("English", metadata.locales.get(0).displayName());
    }

    @Test
    void unknownLocaleNamesRemainStableCodes() {
        KStoryLocale locale = new KStoryLocale("zz-unknown", false, true);

        assertEquals("zz-unknown", locale.displayName());
        assertEquals("zz-unknown (zz-unknown)", locale.toString());
    }
}
