/**
 * Static metadata about each listing source: the country where the vehicle
 * is physically located (rendered as a flag emoji) and whether the source
 * has been checked/verified. The country is a property of the source site,
 * not of each listing, so it is hard-coded here instead of coming from the
 * API.
 */

export interface SourceMeta {
  /** ISO 3166-1 alpha-2 country code, or null when the origin is unknown. */
  countryCode: string | null;
  /** Flag emoji rendered next to the source name. */
  flag: string;
  /** Translation key of the country name, or null when unknown. */
  countryNameKey: string | null;
  /** Whether the source has been checked (green badge), false otherwise. */
  verified: boolean;
}

const SOURCE_META: Record<string, SourceMeta> = {
  AutoExportMarseille: {
    countryCode: 'FR',
    flag: '🇫🇷',
    countryNameKey: 'country.fr',
    verified: true
  },
  CarXExport: {
    countryCode: 'SE',
    flag: '🇸🇪',
    countryNameKey: 'country.se',
    verified: true
  }
};

function sourceMetaOf(source: string): SourceMeta | undefined {
  return SOURCE_META[source];
}

export function getSourceFlag(source: string): string {
  return sourceMetaOf(source)?.flag ?? '';
}

export function getSourceCountryNameKey(source: string): string | null {
  return sourceMetaOf(source)?.countryNameKey ?? null;
}

export function isSourceVerified(source: string): boolean {
  return sourceMetaOf(source)?.verified ?? false;
}

export function getSourceCountryCode(source: string): string | null {
  return sourceMetaOf(source)?.countryCode ?? null;
}