export type Lang = 'en' | 'fr' | 'ar';

type Dictionary = Record<string, string>;

export const TRANSLATIONS: Record<Lang, Dictionary> = {
  en: {
    'app.title': 'Car Export Algeria',
    'app.subtitle': 'Comparison tool for vehicles under 3 years old',

    'search.brandPlaceholder': 'Brand (e.g. Peugeot)',
    'search.modelPlaceholder': 'Model (e.g. 308)',
    'search.maxPricePlaceholder': 'Max price (€)',
    'search.button': 'Search',
    'search.loading': 'Searching...',
    'search.noResults': 'No vehicles found for these criteria.',
    'search.bestPrice': 'Best price',
    'search.source': 'Source:',
    'search.viewListing': 'View listing',
    'search.customsDiscount': 'Customs discount:',

    'discountReason.ELECTRIC': 'Electric vehicles get an 80% customs duty reduction.',
    'discountReason.DIESEL_NOT_ELIGIBLE': 'Diesel vehicles are not eligible for private import.',
    'discountReason.SMALL_ENGINE': '{fuel} engines up to {threshold} cm³ (this one: {displacement} cm³) qualify for this discount.',
    'discountReason.LARGE_ENGINE': '{fuel} engines over {threshold} cm³ (this one: {displacement} cm³) qualify for this discount.',

    'fuel.ESSENCE': 'Essence',
    'fuel.HYBRIDE': 'Hybrid',
    'fuel.ELECTRIQUE': 'Electric',
    'fuel.DIESEL': 'Diesel',

    'currency.title': 'Currency converter',
    'currency.loading': 'Loading exchange rates...',
    'currency.official': "Official (Banque d'Algerie)",
    'currency.parallel': 'Parallel market',
    'currency.rateInfoOfficial': 'official rate',
    'currency.rateInfoParallel': 'parallel market rate',
    'currency.disclaimer': 'Indicative rates, manually configured — not a live feed. Verify against an official source before any transaction.',

    'shipping.title': 'RoRo shipping cost estimator',
    'shipping.button': 'Estimate',
    'shipping.loading': 'Calculating...',
    'shipping.baseFreight': 'Base freight',
    'shipping.handlingFee': 'Handling fee',
    'shipping.total': 'Total',
    'shipping.disclaimer': 'Indicative rates for planning purposes — actual carrier quotes may vary.',

    'errors.generic': 'An error occurred.',
    'errors.ratesLoad': 'Could not load exchange rates.',
    'errors.shippingEstimate': 'Could not estimate shipping cost.'
  },

  fr: {
    'app.title': 'Export Voitures Algérie',
    'app.subtitle': 'Outil de comparaison pour véhicules de moins de 3 ans',

    'search.brandPlaceholder': 'Marque (ex: Peugeot)',
    'search.modelPlaceholder': 'Modèle (ex: 308)',
    'search.maxPricePlaceholder': 'Prix max (€)',
    'search.button': 'Rechercher',
    'search.loading': 'Recherche en cours...',
    'search.noResults': 'Aucun véhicule trouvé pour ces critères.',
    'search.bestPrice': 'Meilleur prix',
    'search.source': 'Source :',
    'search.viewListing': "Voir l'annonce",
    'search.customsDiscount': 'Réduction douanière :',

    'discountReason.ELECTRIC': "Les véhicules électriques bénéficient d'une réduction de 80% des droits de douane.",
    'discountReason.DIESEL_NOT_ELIGIBLE': "Les véhicules diesel ne sont pas éligibles à l'importation par les particuliers.",
    'discountReason.SMALL_ENGINE': 'Les moteurs {fuel} jusqu\'à {threshold} cm³ (ici : {displacement} cm³) bénéficient de cette réduction.',
    'discountReason.LARGE_ENGINE': 'Les moteurs {fuel} au-delà de {threshold} cm³ (ici : {displacement} cm³) bénéficient de cette réduction.',

    'fuel.ESSENCE': 'Essence',
    'fuel.HYBRIDE': 'Hybride',
    'fuel.ELECTRIQUE': 'Électrique',
    'fuel.DIESEL': 'Diesel',

    'currency.title': 'Convertisseur de devises',
    'currency.loading': 'Chargement des taux de change...',
    'currency.official': "Officiel (Banque d'Algérie)",
    'currency.parallel': 'Marché parallèle',
    'currency.rateInfoOfficial': 'taux officiel',
    'currency.rateInfoParallel': 'taux du marché parallèle',
    'currency.disclaimer': "Taux indicatifs, configurés manuellement — pas de flux en temps réel. Vérifiez auprès d'une source officielle avant toute transaction.",

    'shipping.title': 'Estimateur de coût de transport RoRo',
    'shipping.button': 'Estimer',
    'shipping.loading': 'Calcul en cours...',
    'shipping.baseFreight': 'Fret de base',
    'shipping.handlingFee': 'Frais de manutention',
    'shipping.total': 'Total',
    'shipping.disclaimer': 'Tarifs indicatifs à titre de planification — les devis réels du transporteur peuvent varier.',

    'errors.generic': 'Une erreur est survenue.',
    'errors.ratesLoad': 'Impossible de charger les taux de change.',
    'errors.shippingEstimate': "Impossible d'estimer le coût de transport."
  },

  ar: {
    'app.title': 'تصدير السيارات إلى الجزائر',
    'app.subtitle': 'أداة مقارنة للسيارات التي يقل عمرها عن 3 سنوات',

    'search.brandPlaceholder': 'الماركة (مثال: بيجو)',
    'search.modelPlaceholder': 'الطراز (مثال: 308)',
    'search.maxPricePlaceholder': 'السعر الأقصى (€)',
    'search.button': 'بحث',
    'search.loading': 'جارٍ البحث...',
    'search.noResults': 'لم يتم العثور على أي سيارة تطابق هذه المعايير.',
    'search.bestPrice': 'أفضل سعر',
    'search.source': 'المصدر:',
    'search.viewListing': 'عرض الإعلان',
    'search.customsDiscount': 'تخفيض الرسوم الجمركية:',

    'discountReason.ELECTRIC': 'تستفيد السيارات الكهربائية من تخفيض 80% على الرسوم الجمركية.',
    'discountReason.DIESEL_NOT_ELIGIBLE': 'سيارات الديزل غير مؤهلة للاستيراد من قبل الأفراد.',
    'discountReason.SMALL_ENGINE': 'محركات {fuel} حتى {threshold} سم³ (هذه السيارة: {displacement} سم³) تستفيد من هذا التخفيض.',
    'discountReason.LARGE_ENGINE': 'محركات {fuel} فوق {threshold} سم³ (هذه السيارة: {displacement} سم³) تستفيد من هذا التخفيض.',

    'fuel.ESSENCE': 'بنزين',
    'fuel.HYBRIDE': 'هجين',
    'fuel.ELECTRIQUE': 'كهربائية',
    'fuel.DIESEL': 'ديزل',

    'currency.title': 'محول العملات',
    'currency.loading': 'جارٍ تحميل أسعار الصرف...',
    'currency.official': 'رسمي (بنك الجزائر)',
    'currency.parallel': 'السوق الموازي',
    'currency.rateInfoOfficial': 'السعر الرسمي',
    'currency.rateInfoParallel': 'سعر السوق الموازي',
    'currency.disclaimer': 'أسعار إرشادية، يتم تحديثها يدويًا — وليست بثًا مباشرًا. يرجى التحقق من مصدر رسمي قبل أي معاملة.',

    'shipping.title': 'أداة تقدير تكلفة الشحن RoRo',
    'shipping.button': 'تقدير',
    'shipping.loading': 'جارٍ الحساب...',
    'shipping.baseFreight': 'تكلفة الشحن الأساسية',
    'shipping.handlingFee': 'رسوم المناولة',
    'shipping.total': 'الإجمالي',
    'shipping.disclaimer': 'أسعار إرشادية لأغراض التخطيط — قد تختلف عروض أسعار الناقل الفعلية.',

    'errors.generic': 'حدث خطأ.',
    'errors.ratesLoad': 'تعذر تحميل أسعار الصرف.',
    'errors.shippingEstimate': 'تعذر تقدير تكلفة الشحن.'
  }
};
