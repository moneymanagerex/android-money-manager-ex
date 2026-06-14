---
name: MMEX Evolution
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#3c4a42'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#6c7a71'
  outline-variant: '#bbcabf'
  surface-tint: '#006c49'
  primary: '#006c49'
  on-primary: '#ffffff'
  primary-container: '#10b981'
  on-primary-container: '#00422b'
  inverse-primary: '#4edea3'
  secondary: '#2b6954'
  on-secondary: '#ffffff'
  secondary-container: '#adedd3'
  on-secondary-container: '#306d58'
  tertiary: '#52625c'
  on-tertiary: '#ffffff'
  tertiary-container: '#96a69f'
  on-tertiary-container: '#2d3c37'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#6ffbbe'
  primary-fixed-dim: '#4edea3'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#b0f0d6'
  secondary-fixed-dim: '#95d3ba'
  on-secondary-fixed: '#002117'
  on-secondary-fixed-variant: '#0b513d'
  tertiary-fixed: '#d5e6df'
  tertiary-fixed-dim: '#bacac3'
  on-tertiary-fixed: '#101e1a'
  on-tertiary-fixed-variant: '#3b4a44'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Manrope
    fontSize: 48px
    fontWeight: '800'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Manrope
    fontSize: 24px
    fontWeight: '700'
    lineHeight: '1.3'
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Manrope
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.4'
  body-lg:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label-md:
    fontFamily: Manrope
    fontSize: 12px
    fontWeight: '600'
    lineHeight: '1'
    letterSpacing: 0.05em
  data-tabular:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '500'
    lineHeight: '1.2'
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 48px
  gutter: 20px
  margin: 32px
---

## Brand & Style

The design system is engineered for the modern personal finance enthusiast who demands precision without the traditional aesthetic of legacy banking software. The brand personality is **composed, analytical, and refreshing**. It evolves the MMEX legacy by transitioning from a utility-first interface to a sophisticated financial dashboard that balances dense data visualization with a breathable, modern layout.

The visual style is **Corporate Modern with a Minimalist lean**. It prioritizes clarity and information density through a systematic application of whitespace and a restricted color palette. By utilizing "MMEX Green" as the core anchor, the UI evokes feelings of growth, stability, and fiscal health, moving away from "accounting gray" toward a more vibrant, digital-native financial experience.

## Colors

The palette is anchored by **Emerald Green (#10B981)**, used strategically for primary actions and success states. To provide depth and professional "weight," **Forest Green (#064E3B)** is utilized for hover states and high-contrast navigational elements.

Backgrounds leverage a **Soft Mint (#ECFDF5)** tint to reduce eye strain during long data-entry sessions, creating a distinct "paper-like" quality that feels fresher than standard white. Text scales use **Slate (#475569 to #1E293B)** to ensure high legibility without the harshness of pure black. Surface neutrals are kept to **Light Gray (#F8FAFC)** to distinguish containers from the mint-tinted workspace.

## Typography

The typography system uses **Manrope** exclusively to leverage its modern, geometric construction and excellent legibility in data-heavy contexts. 

- **Headlines:** Use tighter letter spacing and heavier weights (Bold/ExtraBold) to create a strong hierarchy.
- **Body:** Set to Medium/Regular with generous line height to maintain readability in transaction descriptions.
- **Data Display:** For financial figures and currency, utilize Manrope’s **tabular lining figures** to ensure numbers align perfectly in vertical columns, which is essential for auditability and comparison.
- **Labels:** Use uppercase with slight tracking for metadata and table headers.

## Layout & Spacing

This design system employs a **12-column fluid grid** for dashboard views and a **centered fixed container** for reporting and settings. The system follows a **4px baseline grid** to ensure all elements—from icons to line heights—align with mathematical precision.

- **Margins:** 32px on desktop to provide a professional "frame."
- **Gutters:** 20px to separate data cards and table columns without fragmenting the layout.
- **Rhythm:** Use `md (16px)` as the standard padding for cards and `sm (8px)` for internal component spacing (e.g., icon-to-text).

## Elevation & Depth

To maintain a "clean and professional" look, this design system avoids heavy shadows. Depth is communicated through **Tonal Layering** and **Subtle Elevation**:

1.  **Level 0 (Base):** Soft Mint (#ECFDF5) background.
2.  **Level 1 (Surfaces):** Pure White (#FFFFFF) cards with a 1px border in Light Gray (#E2E8F0).
3.  **Level 2 (Interaction):** When an item is active or hovered, apply a very soft, diffused shadow: `0px 4px 12px rgba(16, 185, 129, 0.08)`. This uses the primary green as a tint for the shadow to maintain brand harmony.
4.  **Overlays:** Modals use a heavier blur and a Slate-tinted backdrop (40% opacity) to pull focus.

## Shapes

The shape language is defined by **Medium Roundedness**. This provides a approachable, modern feel that softens the "sharpness" of financial data without appearing overly casual or juvenile.

- **Standard Components (Buttons, Inputs):** 8px (0.5rem) corner radius.
- **Cards and Containers:** 16px (1rem) corner radius.
- **Status Badges:** Fully rounded (pill) to distinguish them from actionable buttons.
- **Icons:** Use a 2px stroke weight with rounded caps and joins to match the Manrope font terminals.

## Components

### Buttons
- **Primary:** Forest Green background with White text. Use 8px rounded corners.
- **Secondary:** Transparent background with Emerald Green border and text.
- **Ghost:** Light Gray text that shifts to Forest Green on hover.

### Inputs & Fields
- **Search/Text:** White background, 1px Light Gray border. On focus, the border transitions to Emerald Green with a 2px "soft glow" (ring).
- **Labels:** Positioned above the field in Slate-500, using the `label-md` typography style.

### Tables & Lists
- **Rows:** Alternating subtle mint tint for every second row (zebra striping).
- **Cells:** Vertical alignment should be centered. Currency columns must be right-aligned for easier comparison.
- **Dividers:** Use 1px #F1F5F9 for horizontal separation.

### Cards (The Core Container)
- All dashboard widgets reside in cards. 
- Cards must have a 1px #E2E8F0 border.
- Headers within cards should use a 16px padding and include a "More" action icon in the top right.

### Additional Elements
- **Progress Bars:** Use Emerald Green for the "filled" state and a light version of the same hue for the track.
- **Data Chips:** Small, pill-shaped indicators for categories (e.g., "Housing," "Auto") using a light mint background and dark green text.