package br.com.eterniaserver.eterniashop.baseobjects;

import br.com.eterniaserver.eternialib.core.interfaces.CommandConfirmable;

public class ShopConfirmation implements CommandConfirmable {

    private final Runnable runnable;

    public ShopConfirmation(final Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void execute() {
        runnable.run();
    }

}
