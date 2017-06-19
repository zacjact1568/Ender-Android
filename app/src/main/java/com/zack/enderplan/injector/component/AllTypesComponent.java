package com.zack.enderplan.injector.component;

import com.zack.enderplan.injector.module.AllTypesPresenterModule;
import com.zack.enderplan.injector.scope.FragmentScope;
import com.zack.enderplan.view.fragment.AllTypesFragment;

import dagger.Component;

@FragmentScope
@Component(dependencies = AppComponent.class, modules = AllTypesPresenterModule.class)
public interface AllTypesComponent {

    void inject(AllTypesFragment allTypesFragment);
}
