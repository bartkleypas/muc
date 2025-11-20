package org.kleypas.muc.illustrator

/**
 * Enumerates the supported image styles used by {@link Illustrator}.
 *
 * <p>Supported values:</p>
 * <ul>
 *   <li>{@code PORTRAIT} – vertical orientation (height > width).</li>
 *   <li>{@code LANDSCAPE} – horizontal orientation (width > height).</li>
 *   <li>{@code SQUARE} – equal width and height.</li>
 * </ul>
 */
enum ImageType {
    PORTRAIT,
    LANDSCAPE,
    SQUARE
}
