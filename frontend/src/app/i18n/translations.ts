export type Lang = 'en' | 'fr' | 'ar';

type Dictionary = Record<string, string>;

export const TRANSLATIONS: Record<Lang, Dictionary> = {
  en: {
    'app.title': 'Car Export Algeria',
    'app.subtitle': 'Comparison tool for vehicles under 3 years old',

    'search.brandPlaceholder': 'Brand (e.g. Peugeot)',
    'search.modelPlaceholder': 'Model (e.g. 308)',
    'search.maxPricePlaceholder': 'Max price (€)',
    'search.maxMileagePlaceholder': 'Max mileage (km)',
    'search.cityPlaceholder': 'City',
    'search.fuelTypeAll': 'All fuel types',
    'search.button': 'Search',
    'search.loading': 'Searching...',
    'search.noResults': 'No vehicles found for these criteria.',
    'search.bestPrice': 'Best price',
    'search.source': 'Source:',
    'search.viewListing': 'View listing',
    'search.estimateShipping': 'Estimate shipping cost',

    'pagination.previous': 'Previous',
    'pagination.next': 'Next',
    'pagination.page': 'Page {page} / {total}',

    'modal.edit': 'Edit',
    'modal.cancel': 'Cancel',
    'modal.update': 'Update',
    'search.customsDiscount': 'Customs discount:',

    'discountReason.ELECTRIC': 'Electric vehicles get an 80% customs duty reduction.',
    'discountReason.DIESEL_NOT_ELIGIBLE': 'Diesel vehicles are not eligible for private import.',
    'discountReason.SMALL_ENGINE': '{fuel} engines up to {threshold} cm³ (this one: {displacement} cm³) qualify for this discount.',
    'discountReason.LARGE_ENGINE': '{fuel} engines over {threshold} cm³ (this one: {displacement} cm³) qualify for this discount.',

    'fuel.ESSENCE': 'Essence',
    'fuel.HYBRIDE': 'Hybrid',
    'fuel.ELECTRIQUE': 'Electric',
    'fuel.DIESEL': 'Diesel',


    'shipping.title': 'RoRo shipping cost estimator',
    'shipping.button': 'Estimate',
    'shipping.loading': 'Calculating...',
    'shipping.baseFreight': 'Base freight',
    'shipping.handlingFee': 'Handling fee',
    'shipping.total': 'Total',
    'shipping.disclaimer': 'Indicative rates for planning purposes — actual carrier quotes may vary.',

    'errors.generic': 'An error occurred.',
    'errors.ratesLoad': 'Could not load exchange rates.',
    'errors.shippingEstimate': 'Could not estimate shipping cost.',

    'footer.tagline': 'Compare used cars under 3 years old ready to export to Algeria: price, customs discount and sea freight estimate.',
    'footer.disclaimer': 'Prices and eligibility are indicative. Customs regulations may change.',
    'footer.contact': 'Contact',
    'footer.email': 'Send an email',
    'footer.whatsapp': 'WhatsApp',
    'footer.feedback': 'Leave feedback',
    'footer.sources': 'Listing sources',
    'footer.sourceCarXport': 'CarXport – Sweden',
    'footer.sourceExportCar213': 'ExportCar213',
    'footer.rights': '© {year} {app}. All rights reserved.'
  },

  fr: {
    'app.title': 'Export Voitures Algérie',
    'app.subtitle': 'Outil de comparaison pour véhicules de moins de 3 ans',

    'search.brandPlaceholder': 'Marque (ex: Peugeot)',
    'search.modelPlaceholder': 'Modèle (ex: 308)',
    'search.maxPricePlaceholder': 'Prix max (€)',
    'search.maxMileagePlaceholder': 'Kilométrage max (km)',
    'search.cityPlaceholder': 'Ville',
    'search.fuelTypeAll': 'Toutes motorisations',
    'search.button': 'Rechercher',
    'search.loading': 'Recherche en cours...',
    'search.noResults': 'Aucun véhicule trouvé pour ces critères.',
    'search.bestPrice': 'Meilleur prix',
    'search.source': 'Source :',
    'search.viewListing': "Voir l'annonce",
    'search.estimateShipping': 'Estimer le coût de transport',

    'pagination.previous': 'Précédent',
    'pagination.next': 'Suivant',
    'pagination.page': 'Page {page} / {total}',

    'modal.edit': 'Modifier',
    'modal.cancel': 'Annuler',
    'modal.update': 'Mettre à jour',
    'search.customsDiscount': 'Réduction douanière :',

    'discountReason.ELECTRIC': "Les véhicules électriques bénéficient d'une réduction de 80% des droits de douane.",
    'discountReason.DIESEL_NOT_ELIGIBLE': "Les véhicules diesel ne sont pas éligibles à l'importation par les particuliers.",
    'discountReason.SMALL_ENGINE': 'Les moteurs {fuel} jusqu\'à {threshold} cm³ (ici : {displacement} cm³) bénéficient de cette réduction.',
    'discountReason.LARGE_ENGINE': 'Les moteurs {fuel} au-delà de {threshold} cm³ (ici : {displacement} cm³) bénéficient de cette réduction.',

    'fuel.ESSENCE': 'Essence',
    'fuel.HYBRIDE': 'Hybride',
    'fuel.ELECTRIQUE': 'Électrique',
    'fuel.DIESEL': 'Diesel',


    'shipping.title': 'Estimateur de coût de transport RoRo',
    'shipping.button': 'Estimer',
    'shipping.loading': 'Calcul en cours...',
    'shipping.baseFreight': 'Fret de base',
    'shipping.handlingFee': 'Frais de manutention',
    'shipping.total': 'Total',
    'shipping.disclaimer': 'Tarifs indicatifs à titre de planification — les devis réels du transporteur peuvent varier.',

    'errors.generic': 'Une erreur est survenue.',
    'errors.ratesLoad': 'Impossible de charger les taux de change.',
    'errors.shippingEstimate': "Impossible d'estimer le coût de transport.",

    'footer.tagline': "Comparez des véhicules de moins de 3 ans, éligibles à l'importation vers l'Algérie : prix, réduction douanière et estimation du transport maritime.",
    'footer.disclaimer': "Les prix et l'éligibilité sont indicatifs. La réglementation douanière peut évoluer.",
    'footer.contact': 'Contact',
    'footer.email': 'Envoyer un e-mail',
    'footer.whatsapp': 'WhatsApp',
    'footer.feedback': 'Laisser un avis',
    'footer.sources': 'Sources des annonces',
    'footer.sourceCarXport': 'CarXport – Suède',
    'footer.sourceExportCar213': 'ExportCar213',
    'footer.rights': '© {year} {app}. Tous droits réservés.'
  },

  ar: {
    'app.title': 'تصدير السيارات إلى الجزائر',
    'app.subtitle': 'أداة مقارنة للسيارات التي يقل عمرها عن 3 سنوات',

    'search.brandPlaceholder': 'الماركة (مثال: بيجو)',
    'search.modelPlaceholder': 'الطراز (مثال: 308)',
    'search.maxPricePlaceholder': 'السعر الأقصى (€)',
    'search.maxMileagePlaceholder': 'أقصى مسافة مقطوعة (كم)',
    'search.cityPlaceholder': 'المدينة',
    'search.fuelTypeAll': 'جميع أنواع الوقود',
    'search.button': 'بحث',
    'search.loading': 'جارٍ البحث...',
    'search.noResults': 'لم يتم العثور على أي سيارة تطابق هذه المعايير.',
    'search.bestPrice': 'أفضل سعر',
    'search.source': 'المصدر:',
    'search.viewListing': 'عرض الإعلان',
    'search.estimateShipping': 'تقدير تكلفة الشحن',

    'pagination.previous': 'السابق',
    'pagination.next': 'التالي',
    'pagination.page': 'الصفحة {page} / {total}',

    'modal.edit': 'تعديل',
    'modal.cancel': 'إلغاء',
    'modal.update': 'تحديث',
    'search.customsDiscount': 'تخفيض الرسوم الجمركية:',

    'discountReason.ELECTRIC': 'تستفيد السيارات الكهربائية من تخفيض 80% على الرسوم الجمركية.',
    'discountReason.DIESEL_NOT_ELIGIBLE': 'سيارات الديزل غير مؤهلة للاستيراد من قبل الأفراد.',
    'discountReason.SMALL_ENGINE': 'محركات {fuel} حتى {threshold} سم³ (هذه السيارة: {displacement} سم³) تستفيد من هذا التخفيض.',
    'discountReason.LARGE_ENGINE': 'محركات {fuel} فوق {threshold} سم³ (هذه السيارة: {displacement} سم³) تستفيد من هذا التخفيض.',

    'fuel.ESSENCE': 'بنزين',
    'fuel.HYBRIDE': 'هجين',
    'fuel.ELECTRIQUE': 'كهربائية',
    'fuel.DIESEL': 'ديزل',


    'shipping.title': 'أداة تقدير تكلفة الشحن RoRo',
    'shipping.button': 'تقدير',
    'shipping.loading': 'جارٍ الحساب...',
    'shipping.baseFreight': 'تكلفة الشحن الأساسية',
    'shipping.handlingFee': 'رسوم المناولة',
    'shipping.total': 'الإجمالي',
    'shipping.disclaimer': 'أسعار إرشادية لأغراض التخطيط — قد تختلف عروض أسعار الناقل الفعلية.',

    'errors.generic': 'حدث خطأ.',
    'errors.ratesLoad': 'تعذر تحميل أسعار الصرف.',
    'errors.shippingEstimate': 'تعذر تقدير تكلفة الشحن.',

    'footer.tagline': 'قارن السيارات المستعملة التي يقل عمرها عن 3 سنوات والجاهزة للتصدير إلى الجزائر: السعر، التخفيض الجمركي، وتقدير تكلفة النقل البحري.',
    'footer.disclaimer': 'الأسعار والجدارة بالاستيراد إرشادية. قد تتغير اللوائح الجمركية.',
    'footer.contact': 'اتصل بنا',
    'footer.email': 'إرسال بريد إلكتروني',
    'footer.whatsapp': 'واتساب',
    'footer.feedback': 'أرسل تقييمك',
    'footer.sources': 'مصادر الإعلانات',
    'footer.sourceCarXport': 'CarXport – السويد',
    'footer.sourceExportCar213': 'ExportCar213',
    'footer.rights': 'جميع الحقوق محفوظة © {year} {app}.'
  }
};
