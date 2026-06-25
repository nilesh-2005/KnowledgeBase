---
version: 1.0.0
name: Enterprise-SaaS-Design
description: |
  A dense, professional, and utilitarian enterprise SaaS dashboard design system inspired by Linear, GitHub, and Notion. This system prioritizes clarity, information density, typography, and usability. It uses a neutral grayscale palette with a single primary accent color, subtle shadows for elevation, and 4px-8px border radii.

colors:
  primary: "#111827" # Gray 900
  on-primary: "#ffffff"
  canvas: "#f9fafb" # Gray 50
  surface: "#ffffff"
  surface-hover: "#f3f4f6" # Gray 100
  border: "#e5e7eb" # Gray 200
  border-focus: "#3b82f6" # Blue 500
  text-main: "#111827" # Gray 900
  text-muted: "#6b7280" # Gray 500
  success: "#10b981" # Emerald 500
  error: "#ef4444" # Red 500
  warning: "#f59e0b" # Amber 500

typography:
  fontFamily: "Inter, Helvetica, sans-serif"
  sizes:
    xs: "12px"
    sm: "14px"
    base: "16px"
    lg: "18px"
    xl: "20px"
    2xl: "24px"
  weights:
    normal: 400
    medium: 500
    semibold: 600

spacing:
  xs: "4px"
  sm: "8px"
  md: "12px"
  lg: "16px"
  xl: "24px"
  2xl: "32px"

rounded:
  none: "0px"
  sm: "4px"
  md: "6px"
  lg: "8px"
  full: "9999px"

shadows:
  sm: "0 1px 2px 0 rgba(0, 0, 0, 0.05)"
  md: "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)"

components:
  card:
    backgroundColor: "{colors.surface}"
    borderColor: "{colors.border}"
    borderRadius: "{rounded.lg}"
    shadow: "{shadows.sm}"
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    borderRadius: "{rounded.md}"
    padding: "8px 16px"
    shadow: "{shadows.sm}"
  button-secondary:
    backgroundColor: "{colors.surface}"
    borderColor: "{colors.border}"
    textColor: "{colors.text-main}"
    borderRadius: "{rounded.md}"
    padding: "8px 16px"
    shadow: "{shadows.sm}"
---

## Overview

This design system is built for enterprise productivity applications. It favors high information density, clear typographic hierarchy, and subtle visual cues over flashy aesthetics. The layout typically features a fixed left sidebar for navigation and a top bar for contextual actions.

## Typography
We use **Inter** (or a system sans-serif fallback) to ensure legibility at small sizes. The base UI text size is `14px` (sm) to allow for dense data displays without feeling cramped.
- `12px` (xs): Metadata, small badges, fine print.
- `14px` (sm): Base body text, table cells, buttons, inputs.
- `16px` (base): Emphasized body text, section subheadings.
- `20px` (xl) - `24px` (2xl): Page and major section headings.

## Colors
The palette is primarily composed of cool grays.
- **Canvas (`#f9fafb`)**: Used for the main application background.
- **Surface (`#ffffff`)**: Used for cards, modals, and dropdowns.
- **Border (`#e5e7eb`)**: Used extensively to separate UI areas (sidebar, topnav, table rows, cards).
- **Text**: `#111827` for primary readability, `#6b7280` for secondary/muted information.

## Spacing & Density
Components are compact. Padding within cards and tables should default to `16px` or `24px`, while spacing between items in lists or small component groups should be `8px` or `12px`.

## UI Components
- **Buttons**: Should be compact (e.g., 32px or 36px height), with `rounded-md` (6px) corners. Primary buttons are dark gray/black. Secondary buttons are white with a gray border.
- **Cards**: White surface, 1px gray border, `rounded-lg` (8px), and a very subtle `shadow-sm`.
- **Inputs**: 1px gray border, 14px text, with a blue or dark focus ring.
