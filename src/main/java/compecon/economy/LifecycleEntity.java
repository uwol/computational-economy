package compecon.economy;

/**
 * an entity with a lifecycle, which is initialized and deconstructed in a
 * controlled manner.
 */
public interface LifecycleEntity {

	public void initialize();

	public boolean isDeconstructed();

	public void deconstruct();
}
