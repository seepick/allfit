package allfit.sync

import allfit.api.OnefitClient
import allfit.api.models.CategoriesJson
import allfit.api.models.CategoryJsonDefinition
import allfit.api.models.PartnersJson
import allfit.persistence.domain.CategoriesRepo
import allfit.persistence.domain.CategoryEntity
import mu.KotlinLogging.logger

interface CategoriesSyncer {
    suspend fun sync(partners: PartnersJson)
}

class CategoriesSyncerImpl(
    private val client: OnefitClient,
    private val categoriesRepo: CategoriesRepo,
) : CategoriesSyncer {
    private val log = logger {}

    override suspend fun sync(partners: PartnersJson) {
        log.debug { "Syncing categories ..." }
        syncAny(categoriesRepo, mergedCategories(client.getCategories(), partners)) {
            it.toCategoryEntity()
        }
    }
}

fun CategoryJsonDefinition.toCategoryEntity() = CategoryEntity(
    id = id,
    name = name,
    isDeleted = false,
    slug = slugs?.en,
)

private fun mergedCategories(categories: CategoriesJson, partners: PartnersJson) =
    mutableMapOf<Int, CategoryJsonDefinition>().apply {
        putAll(partners.toFlattenedCategories().associateBy { it.id })
        // partner has less data, so overwrite with categories (use at last)
        putAll(categories.data.associateBy { it.id })
    }.values.toList()

private fun PartnersJson.toFlattenedCategories() = data.map { partner ->
    mutableListOf<CategoryJsonDefinition>().also {
        it.add(partner.category)
        it.addAll(partner.categories)
    }
}.flatten()
