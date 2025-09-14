package bodyhealth.migrations;

import bodyhealth.Main;

public abstract class Migration {

    public void onLoad(Main main) {}

    public void onEnable(Main main) {}

    public void onReload(Main main) {}

    public void onDisable(Main main) {}

}
