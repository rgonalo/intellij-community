package com.intellij.uiDesigner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.uiDesigner.componentTree.ComponentPtr;
import com.intellij.uiDesigner.componentTree.ComponentSelectionListener;
import com.intellij.uiDesigner.designSurface.GuiEditor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Stack;

/**
 * This class implements Ctrl+W / Ctrl+Shift+W functionality in the GuiEditor
 *
 * @author Anton Katilin
 * @author Vladimir Kondratyev
 */
public final class SelectionState{
  private final Stack<ComponentPtr[]> mySelectionHistory;
  /** We do not need to handle our own events */
  private boolean myInsideChange;

  public SelectionState(@NotNull final GuiEditor editor) {
    //noinspection ConstantConditions
    if (editor == null) {
      throw new IllegalArgumentException("editor cannot be null");
    }
    mySelectionHistory = new Stack<ComponentPtr[]>();
    editor.addComponentSelectionListener(new MyComponentSelectionListener());
  }

  public boolean isInsideChange(){
    ApplicationManager.getApplication().assertIsDispatchThread();
    return myInsideChange;
  }

  public void setInsideChange(final boolean insideChange){
    ApplicationManager.getApplication().assertIsDispatchThread();
    myInsideChange = insideChange;
  }

  public Stack<ComponentPtr[]> getSelectionHistory() {
    return mySelectionHistory;
  }

  public static ComponentPtr[] getSelection(final GuiEditor editor){
    final ArrayList<RadComponent> selection = FormEditingUtil.getAllSelectedComponents(editor);
    final ComponentPtr[] ptrs = new ComponentPtr[selection.size()];
    for(int i = selection.size() - 1; i >= 0; i--){
      ptrs[i] = new ComponentPtr(editor, selection.get(i));
    }
    return ptrs;
  }

  public static void restoreSelection(final GuiEditor editor, final ComponentPtr[] ptrs) {
    FormEditingUtil.clearSelection(editor.getRootContainer());
    for(int i = ptrs.length - 1; i >= 0; i--){
      final ComponentPtr ptr = ptrs[i];
      ptr.validate();
      if(ptr.isValid()){
        ptr.getComponent().setSelected(true);
      }
    }
  }

  private final class MyComponentSelectionListener implements ComponentSelectionListener{

    public void selectedComponentChanged(final GuiEditor source) {
      if(myInsideChange){ // do not react on own events
        return;
      }
      mySelectionHistory.clear();
      mySelectionHistory.push(getSelection(source));
    }
  }

}
