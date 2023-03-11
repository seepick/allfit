package allfit.api.models

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

fun Arb.Companion.categoryJson() = arbitrary {
    CategoryJson(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 8).next(),
        slugs = slugJson().next()
    )
}

fun Arb.Companion.slugJson() = arbitrary {
    SlugJson(
        en = string(minSize = 1, maxSize = 8).next(),
        nl = string(minSize = 1, maxSize = 8).orNull().next(),
        es = string(minSize = 1, maxSize = 8).orNull().next(),
    )
}

fun Arb.Companion.partnerJson() = arbitrary {
    PartnerJson(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 8).next(),
        category = partnerCategoryJson().next(),
        categories = list(partnerCategoryJson(), 0..5).next(),
        slug = string(minSize = 1, maxSize = 8).next(),
        description = string(minSize = 1, maxSize = 20).next(),
        header_image = headerImageJson().next(),
        settlement_options = settlementOptionsJson().next(),
        location_groups = list(partnerLocationGroupsJson(), 0..3).next(),
    )
}

fun Arb.Companion.headerImageJson() = arbitrary {
    HeaderImageJson(
        orig = "https://server.test/${
            string(
                minSize = 1,
                maxSize = 20,
                codepoints = Codepoint.alphanumeric()
            ).next()
        }.jpg"
    )
}

fun Arb.Companion.settlementOptionsJson() = arbitrary {
    SettlementOptionsJson(
        drop_in_enabled = boolean().next(),
        reservable_workouts = boolean().next(),
        first_come_first_serve = boolean().next(),
    )
}

fun Arb.Companion.partnerLocationGroupsJson() = arbitrary {
    PartnerLocationGroupsJson(
        latitude = double(min = 0.0).next(),
        longitude = double(min = 0.0).next(),
        locations = list(partnerLocationJson(), 1..3).next(),
    )
}

fun Arb.Companion.partnerLocationJson() = arbitrary {
    PartnerLocationJson(
        street_name = string(minSize = 1, maxSize = 8).next(),
        house_number = string(minSize = 1, maxSize = 8).next(),
        addition = string(minSize = 1, maxSize = 8).next(),
        zip_code = string(minSize = 1, maxSize = 6).next(),
        city = string(minSize = 1, maxSize = 8).next(),
        latitude = double(min = 0.0).next(),
        longitude = double(min = 0.0).next(),
    )
}

fun Arb.Companion.partnerCategoryJson() = arbitrary {
    PartnerCategoryJson(
        id = int(min = 1).next(),
        name = string(minSize = 1, maxSize = 8).next(),
    )
}
