package com.jakewharton.rxbinding2.support.design.widget;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.View.OnClickListener;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

import static io.reactivex.android.MainThreadDisposable.verifyMainThread;

final class SnackbarActionObservable extends Observable<View> {
  final Snackbar view;
  final SetActionStrategy strategy;

  public SnackbarActionObservable(Snackbar view, int resId) {
    this.view = view;
    this.strategy = new ResIdSetActionStrategy(resId);
  }
  public SnackbarActionObservable(Snackbar view, CharSequence text) {
    this.view = view;
    this.strategy = new TextSetActionStrategy(text);
  }

  @Override protected void subscribeActual(Observer<? super View> observer) {
    verifyMainThread();
    Listener listener = new Listener(strategy, observer);
    observer.onSubscribe(listener);
    strategy.setAction(listener.callback);
  }

  final class Listener extends MainThreadDisposable {
    private final SetActionStrategy strategy;
    private final OnClickListener callback;

    Listener(SetActionStrategy strategy, final Observer<? super View> observer) {
      this.strategy = strategy;
      this.callback = new OnClickListener() {
        @Override
        public void onClick(View view) {
          if (!isDisposed()) {
            observer.onNext(view);
          }
        }
      };
    }

    @Override protected void onDispose() {
      strategy.setAction(null);
    }
  }

  interface SetActionStrategy {
    void setAction(OnClickListener listener);
  }

  final class TextSetActionStrategy implements SetActionStrategy {

    private final CharSequence text;

    TextSetActionStrategy(CharSequence text) {
      this.text = text;
    }

    @Override public void setAction(OnClickListener listener) {
      view.setAction(text, listener);
    }
  }

  final class ResIdSetActionStrategy implements SetActionStrategy {

    private final int resId;

    ResIdSetActionStrategy(int resId) {
      this.resId = resId;
    }

    @Override public void setAction(OnClickListener listener) {
      view.setAction(resId, listener);
    }
  }
}
