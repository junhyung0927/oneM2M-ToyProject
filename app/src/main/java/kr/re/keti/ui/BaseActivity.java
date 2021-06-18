package kr.re.keti.ui;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

public abstract class BaseActivity<B extends ViewDataBinding> extends AppCompatActivity {

    protected B binding;

    protected void bind(@LayoutRes int layoutId) {
        binding = DataBindingUtil.setContentView(this, layoutId);
    }
}
