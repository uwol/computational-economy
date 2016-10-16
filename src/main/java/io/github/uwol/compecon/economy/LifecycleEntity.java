package io.github.uwol.compecon.economy;

/**
 * an entity with a lifecycle, which is initialized and deconstructed in a
 * controlled manner.
 */
public interface LifecycleEntity {

	public void deconstruct();

	public void initialize();

	public boolean isDeconstructed();
}
