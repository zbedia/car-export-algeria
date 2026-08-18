/**
 * Maps a brand to its common models, used to narrow the model field's
 * autocomplete suggestions once a brand has been picked. Covers the
 * brands most relevant to this market — not exhaustive for every
 * brand in CAR_BRANDS. When the current brand isn't in this map, the
 * model field falls back to ALL_MODELS below, so suggestions are
 * never empty.
 */
export const CAR_MODELS_BY_BRAND: Record<string, string[]> = {
  'Peugeot': ['108', '208', '2008', '308', '3008', '408', '508', '5008', 'Rifter', 'Partner'],
  'Renault': ['Clio', 'Captur', 'Megane', 'Austral', 'Arkana', 'Kadjar', 'Talisman', 'Scenic', 'Twingo', 'Trafic'],
  'Citroën': ['C3', 'C3 Aircross', 'C4', 'C4 X', 'C5 Aircross', 'C5 X', 'Berlingo', 'SpaceTourer'],
  'DS Automobiles': ['DS 3', 'DS 4', 'DS 7', 'DS 9'],
  'Dacia': ['Sandero', 'Duster', 'Logan', 'Jogger', 'Spring'],
  'Volkswagen': ['Polo', 'Golf', 'T-Roc', 'T-Cross', 'Tiguan', 'Passat', 'Touareg', 'ID.3', 'ID.4', 'Arteon'],
  'BMW': ['Serie 1', 'Serie 2', 'Serie 3', 'Serie 4', 'Serie 5', 'Serie 7', 'X1', 'X3', 'X5', 'X6', 'i4', 'iX'],
  'Mercedes-Benz': ['Classe A', 'Classe B', 'Classe C', 'Classe E', 'Classe S', 'GLA', 'GLB', 'GLC', 'GLE', 'EQA', 'EQC'],
  'Audi': ['A1', 'A3', 'A4', 'A5', 'A6', 'A7', 'A8', 'Q2', 'Q3', 'Q5', 'Q7', 'Q8', 'e-tron'],
  'Toyota': ['Yaris', 'Corolla', 'C-HR', 'RAV4', 'Camry', 'Prius', 'Land Cruiser', 'Hilux', 'Aygo X'],
  'Ford': ['Fiesta', 'Focus', 'Puma', 'Kuga', 'Mondeo', 'EcoSport', 'Ranger', 'Mustang', 'Explorer'],
  'Opel': ['Corsa', 'Astra', 'Crossland', 'Grandland', 'Mokka', 'Insignia', 'Combo'],
  'Fiat': ['500', '500X', 'Panda', 'Tipo', 'Punto', 'Doblo'],
  'Hyundai': ['i10', 'i20', 'i30', 'Kona', 'Tucson', 'Santa Fe', 'Elantra', 'Ioniq 5'],
  'Kia': ['Picanto', 'Rio', 'Ceed', 'Stonic', 'Sportage', 'Sorento', 'Niro', 'EV6'],
  'Nissan': ['Micra', 'Juke', 'Qashqai', 'X-Trail', 'Leaf', 'Navara'],
  'Škoda': ['Fabia', 'Scala', 'Octavia', 'Kamiq', 'Karoq', 'Kodiaq', 'Superb'],
  'Seat': ['Ibiza', 'Leon', 'Arona', 'Ateca', 'Tarraco'],
  'Cupra': ['Formentor', 'Leon', 'Ateca', 'Born'],
  'Volvo': ['V40', 'V60', 'V90', 'XC40', 'XC60', 'XC90'],
  'Mazda': ['Mazda2', 'Mazda3', 'CX-3', 'CX-30', 'CX-5', 'MX-5'],
  'Mitsubishi': ['Space Star', 'ASX', 'Eclipse Cross', 'Outlander', 'L200'],
  'Suzuki': ['Swift', 'Vitara', 'S-Cross', 'Jimny', 'Ignis'],
  'Honda': ['Jazz', 'Civic', 'CR-V', 'HR-V', 'e'],
  'Chevrolet': ['Spark', 'Aveo', 'Cruze', 'Captiva', 'Camaro'],
  'Jeep': ['Renegade', 'Compass', 'Cherokee', 'Grand Cherokee', 'Wrangler'],
  'Land Rover': ['Defender', 'Discovery', 'Discovery Sport', 'Range Rover', 'Range Rover Evoque', 'Range Rover Sport'],
  'Mini': ['Cooper', 'Countryman', 'Clubman'],
  'Alfa Romeo': ['Giulia', 'Giulietta', 'Stelvio', 'Tonale'],
  'Tesla': ['Model 3', 'Model S', 'Model X', 'Model Y'],
  'BYD': ['Atto 3', 'Dolphin', 'Seal', 'Han'],
  'Lada': ['Niva', 'Vesta', 'Granta']
};

/** Flattened, deduplicated, sorted list — used as a fallback when the
 * current brand isn't in CAR_MODELS_BY_BRAND, so suggestions are
 * never empty. */
export const ALL_MODELS: string[] = Array.from(
  new Set(Object.values(CAR_MODELS_BY_BRAND).flat())
).sort((a, b) => a.localeCompare(b));
