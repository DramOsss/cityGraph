package citygraph.ui;

import citygraph.core.AppState;

public interface StateAware {
    void setState(AppState state, Navigator nav);
}