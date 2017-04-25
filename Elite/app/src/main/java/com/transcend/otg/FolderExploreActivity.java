package com.transcend.otg;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.provider.DocumentFile;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.transcend.otg.Adapter.FolderExploreAdapter;
import com.transcend.otg.Adapter.FolderExploreDropDownAdapter;
import com.transcend.otg.Bitmap.IconHelper;
import com.transcend.otg.Browser.BrowserFragment;
import com.transcend.otg.Browser.PagerSwipeRefreshLayout;
import com.transcend.otg.Constant.ActionParameter;
import com.transcend.otg.Constant.Constant;
import com.transcend.otg.Constant.FileInfo;
import com.transcend.otg.Dialog.LocalDecryptDialog;
import com.transcend.otg.Dialog.LocalDeleteDialog;
import com.transcend.otg.Dialog.LocalEncryptDialog;
import com.transcend.otg.Dialog.LocalNewFolderDialog;
import com.transcend.otg.Dialog.LocalRenameDialog;
import com.transcend.otg.Dialog.OTGDecryptDialog;
import com.transcend.otg.Dialog.OTGDeleteDialog;
import com.transcend.otg.Dialog.OTGEncryptDialog;
import com.transcend.otg.Dialog.OTGRenameDialog;
import com.transcend.otg.Dialog.OTGNewFolderDialog;
import com.transcend.otg.Dialog.PreGuideDialog;
import com.transcend.otg.Dialog.SDDecryptDialog;
import com.transcend.otg.Dialog.SDPermissionGuideDialog;
import com.transcend.otg.Loader.FileActionManager;
import com.transcend.otg.Loader.LocalEncryptCopyLoader;
import com.transcend.otg.Loader.LocalEncryptNewFolderLoader;
import com.transcend.otg.Loader.LocalListLoader;
import com.transcend.otg.Loader.OTGCopytoLocalDecryptLoader;
import com.transcend.otg.Loader.OTGCopytoLocalEncryptLoader;
import com.transcend.otg.Loader.OTGDecryptLoader;
import com.transcend.otg.Loader.OTGDecryptNewFolderLoader;
import com.transcend.otg.Loader.OTGEncryptLoader;
import com.transcend.otg.Loader.OTGEncryptNewFolderLoader;
import com.transcend.otg.Loader.OTGFileLoader;
import com.transcend.otg.Loader.SDCopytoLocalDecryptLoader;
import com.transcend.otg.Loader.SDDecryptLoader;
import com.transcend.otg.Loader.SDDecryptNewFolderLoader;
import com.transcend.otg.Loader.SDEncryptCopyLoader;
import com.transcend.otg.Loader.SDEncryptLoader;
import com.transcend.otg.Loader.SDEncryptNewFolderLoader;
import com.transcend.otg.Photo.PhotoActivity;
import com.transcend.otg.Task.ComputeFilsNumberTask;
import com.transcend.otg.Task.ComputeFilsTotalSizeTask;
import com.transcend.otg.Task.SDMoveToSDTask;
import com.transcend.otg.Utils.DecryptUtils;
import com.transcend.otg.Utils.EncryptUtils;
import com.transcend.otg.Utils.FileFactory;
import com.transcend.otg.Utils.FileInfoSort;
import com.transcend.otg.Utils.MediaUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wangbojie on 2017/3/2.
 */

public class FolderExploreActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Boolean>,
        FolderExploreDropDownAdapter.OnDropdownItemSelectedListener,
        FolderExploreAdapter.OnRecyclerItemCallbackListener,
        ActionMode.Callback{

    private String TAG = FolderExploreActivity.class.getSimpleName();
    public static final int REQUEST_CODE = FolderExploreActivity.class.hashCode() & 0xFFFF;
    private FileActionManager mFileActionManager;
    private FileInfo mFile;
    private String mPath;
    private Uri mUri;
    private DocumentFile mDocumentFile, rootDir;
    private int mMode;
    private int mSDDocumentTreeID = 1001, nowAction;
    private ArrayList<FileInfo> mFileList;
    private DocumentFile mCurrentDocumentFile;
    private FolderExploreDropDownAdapter mDropdownAdapter;
    private AppCompatSpinner mDropdown;
    private FolderExploreAdapter mFolderExploreAdapter;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayout;
    private IconHelper mIconHelper;
    private View mEmptyView;
    private Context mContext;
    private HashMap<String, DocumentFile> dropDownMapOTG;
    private ArrayList<String> dropDownListOTG;
    private ActionMode mActionMode;
    private RelativeLayout mActionModeView;
    private TextView mActionModeTitle;
    private FloatingActionButton mFab, mFabExit;
    private PagerSwipeRefreshLayout mSwipeRefreshLayout;
    private CoordinatorLayout mCoordinatorlayout;
    private Toolbar toolbar;
    private static final String ACTION_USB_PERMISSION = "com.transcend.otg.USB_PERMISSION";
    private boolean mInitOtgLoad = true;
    private int mScreenW;
    private Calendar calendar;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
    private LinearLayout loadingContainer;

    //Menu
    private MenuItem.OnMenuItemClickListener mCustomMenuItemClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_explore);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenW = displaymetrics.widthPixels;
        initToolbar();
        initRecyclerViewAndAdapter();
        initDropdown();
        initBroadcast();
        initData();
        initActionModeView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_custom_main, menu);
        MenuItem customMenu = menu.findItem(R.id.more);
        customMenu.setOnMenuItemClickListener(mCustomMenuItemClicked);
        customMenu.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_folder:
                if (mFile != null) {
                    FileInfo fileInfo = mFile;
                    if (fileInfo.storagemode == Constant.STORAGEMODE_LOCAL)
                        doLocalNewFolder();
                    else if (fileInfo.storagemode == Constant.STORAGEMODE_SD) {
                        nowAction = R.id.menu_new_folder;
                        doOTGNewFolder(true);
                    } else if (fileInfo.storagemode == Constant.STORAGEMODE_OTG)
                        doOTGNewFolder(false);
                }
                return true;
            case R.id.menu_grid:
                updateLayout(Constant.ITEM_GRID);
                return true;
            case R.id.menu_list:
                updateLayout(Constant.ITEM_LIST);
                return true;
            case R.id.menu_easy_sort:
                createPopupWindow(toolbar, this);
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar(){
        toolbar = (Toolbar) findViewById(R.id.explore_toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_navigation_arrow_white);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mCustomMenuItemClicked = new CustomMenuItemClicked();
    }

    private void initRecyclerViewAndAdapter() {
        mCoordinatorlayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        mSwipeRefreshLayout = (PagerSwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mActionMode != null)
                    mActionMode.finish();;
                doRefresh();
            }
        });
        mEmptyView = findViewById(R.id.empty_view);
        loadingContainer = (LinearLayout) findViewById(R.id.loading_container);
        int layout_mode = LocalPreferences.getBrowserViewMode(this, BrowserFragment.LIST_TYPE_FOLDER, Constant.ITEM_LIST);
        mIconHelper = new IconHelper(this, layout_mode);
        mFolderExploreAdapter = new FolderExploreAdapter(this, mIconHelper);
        mFolderExploreAdapter.setOnRecyclerItemCallbackListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.explore_recycler_view);


        int ColumnCount = calculateColumnCount(layout_mode);
        mLayout = new GridLayoutManager(this, ColumnCount);
        mLayout.setSpanCount(ColumnCount);
        mRecyclerView.setLayoutManager(mLayout);
        mRecyclerView.setAdapter(mFolderExploreAdapter);
    }

    private void initDropdown() {
        nowAction = -1;
        mDropdownAdapter = new FolderExploreDropDownAdapter(this, false);
        mDropdownAdapter.setOnDropdownItemSelectedListener(this);
        mDropdown = (AppCompatSpinner) findViewById(R.id.main_dropdown);
        mDropdown.setAdapter(mDropdownAdapter);
        mDropdown.setDropDownVerticalOffset(10);
        resetDropDownMapAndList();
    }

    private void initBroadcast(){
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if(Constant.nowMODE == Constant.MODE.OTG)
                    finish();
            }
        }
    };

    private void initData() {
        mContext = this;
        Constant.Activity = 1;
        mFileActionManager = new FileActionManager(this, FileActionManager.MODE.LOCAL, this);
        mFile = Constant.mCurrentFile;
        mPath = mFile.path;
        mMode = mFile.storagemode;
    }

    private void initActionModeView() {
        mFab = (FloatingActionButton) findViewById(R.id.explore_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionMode == null)
                    startActionMode();
                else
                    toggleSelectAll();

            }
        });
        mFabExit = (FloatingActionButton) findViewById(R.id.explore_fab_exit);
        mFabExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mActionModeView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.action_mode_custom, null);
        mActionModeTitle = (TextView) mActionModeView.findViewById(R.id.action_mode_custom_title);
    }

    private void startPhotoSingleView(ArrayList<FileInfo> list, int position) {
        Intent intent = new Intent(this, PhotoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int newListPosition = 0;
        ArrayList<FileInfo> photoList = new ArrayList<FileInfo>();
        for (int i=0;i<list.size();i++) {
            if (list.get(i).type == Constant.TYPE_PHOTO)
                photoList.add(list.get(i));
            if (i == position)
                newListPosition = photoList.size() - 1;
        }

        intent.putParcelableArrayListExtra("photo_list", photoList);
        intent.putExtra("list_index", newListPosition);
        startActivity(intent);
    }

    private void createInfoDialog(Context context, FileInfo fileInfo, int dialog_size) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View mInfoDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        ((TextView) mInfoDialogView.findViewById(R.id.name)).setText(fileInfo.name);
        ((TextView) mInfoDialogView.findViewById(R.id.type)).setText(getFileTypeString(context, fileInfo.type));
        if (fileInfo.format_size == null) {
            ((TextView) mInfoDialogView.findViewById(R.id.size)).setText(Formatter.formatFileSize(context, fileInfo.size));
        } else {
            ((TextView) mInfoDialogView.findViewById(R.id.size)).setText(fileInfo.format_size);
        }
        ((TextView) mInfoDialogView.findViewById(R.id.modify_time)).setText(fileInfo.time);
        ((TextView) mInfoDialogView.findViewById(R.id.path)).setText(fileInfo.path);
        if (fileInfo.type == Constant.TYPE_DIR) {
            mInfoDialogView.findViewById(R.id.file_number_title).setVisibility(View.VISIBLE);
            TextView fileNumView = (TextView) mInfoDialogView.findViewById(R.id.file_number);
            fileNumView.setVisibility(View.VISIBLE);
            fileNumView.setText(context.getResources().getString(R.string.info_file_number_computing));
            new ComputeFilsNumberTask(context, fileInfo, fileNumView).execute();
            new ComputeFilsTotalSizeTask(context, fileInfo, (TextView) mInfoDialogView.findViewById(R.id.size)).execute();
        }

        builder.setView(mInfoDialogView);
        builder.setTitle(context.getResources().getString(R.string.info_title));
        builder.setIcon(R.mipmap.ic_info_gray);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(dialog_size, dialog_size*6/5);
    }

    private String getFileTypeString(Context context, int type) {
        switch (type) {
            case Constant.TYPE_PHOTO:
                return context.getResources().getString(R.string.info_image);
            case Constant.TYPE_MUSIC:
                return context.getResources().getString(R.string.info_music);
            case Constant.TYPE_VIDEO:
                return context.getResources().getString(R.string.info_video);
            case Constant.TYPE_DOC:
                return context.getResources().getString(R.string.info_document);
            case Constant.TYPE_ENCRYPT:
                return context.getResources().getString(R.string.encrypt);
            case Constant.TYPE_DIR:
                return context.getResources().getString(R.string.info_folder);
            default: //Constant.TYPE_OTHER_FILE:
                return context.getResources().getString(R.string.info_other);
        }
    }


    private void doLoad(String path) {
        mFileActionManager.checkServiceMode(path);
        mFileActionManager.list(path);
    }

    private void doLoadOTG(FileInfo file, boolean bInitial) {
        mFileActionManager.setMode(FileActionManager.MODE.OTG);
        if(bInitial){
            resetDropDownMapAndList();
            mFileActionManager.otgList(file);
        }else{
            Constant.mCurrentDocumentFileExplore = mCurrentDocumentFile;
            mFileActionManager.otgList(null);
        }

    }

    private void updateScreen() {
        if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
            mDropdownAdapter.updateList(mPath);
            mDropdownAdapter.notifyDataSetChanged();
            mFolderExploreAdapter.update(mFileList);
            checkEmpty();
        }else if (mMode == Constant.STORAGEMODE_OTG){
            dropDownMapOTG.put(mCurrentDocumentFile.getName(), mCurrentDocumentFile);
            updateDropDownList(mCurrentDocumentFile);
            mDropdownAdapter.updateList(dropDownListOTG);
            mDropdownAdapter.notifyDataSetChanged();
            mFolderExploreAdapter.update(mFileList);
            checkEmpty();
        }
    }

    private void checkEmpty() {
        if (mFileList.isEmpty())
            mEmptyView.setVisibility(View.VISIBLE);
        else
            mEmptyView.setVisibility(View.GONE);
    }

    private void resetDropDownMapAndList() {
        dropDownMapOTG = new HashMap<String, DocumentFile>();
        dropDownListOTG = new ArrayList<String>();

    }

    private void updateDropDownList(DocumentFile mDFile) {
        String fileName = mDFile.getName();
        Uri fileUri = mDFile.getUri();
        if (dropDownListOTG.size() == 0) {//first time, add parent file
            dropDownMapOTG.put(mDFile.getParentFile().getName(), mDFile.getParentFile());
            dropDownListOTG.add(mDFile.getParentFile().getName() + "@" + mDFile.getParentFile().getUri());
            dropDownListOTG.add(fileName + "@" + fileUri.toString());
        } else {//not first time in, should do arrange
            int dSize = dropDownListOTG.size();
            for (int i = 0; i < dSize; i++) {
                String[] compareFileName = dropDownListOTG.get(i).split("@");
                if (compareFileName[0].equals(fileName) && compareFileName[1].equals(fileUri.toString())) {//the same
                    //remove rest dropDownList
                    while (dropDownListOTG.size() > i + 1) {
                        dropDownListOTG.remove(i + 1);
                    }
                    break;
                } else {//not the same
                    if (i == dSize - 1) {//last index, which means there is no same uri
                        dropDownListOTG.add(fileName + "@" + fileUri.toString());
                    }
                }
            }
        }
    }

    private void snackBarShow(int resId) {
        Snackbar.make(mCoordinatorlayout, resId, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private boolean checkSDWritePermission(){
        String uid = FileFactory.getSDCardUniqueId();
        String sdKey = LocalPreferences.getSDKey(mContext, uid);
        if(sdKey != ""){
            Uri uriSDKey = Uri.parse(sdKey);
            Constant.mSDCurrentDocumentFile = Constant.mSDRootDocumentFile = DocumentFile.fromTreeUri(this, uriSDKey);
            return true;
        }else{
            preGuideDialog("sd");
            return false;
        }
    }

    private void preGuideDialog(String type) {
        new PreGuideDialog(this, type){
            @Override
            public void onConfirm(String type) {
                if(type.equals("sd"))
                    intentDocumentTreeSD();
            }

            @Override
            public void onCancel(String type) {

            }
        };
    }

    private void intentDocumentTreeSD() {
        new SDPermissionGuideDialog(this) {
            @Override
            public void onConfirm(Boolean isClick) {
                if (isClick) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, mSDDocumentTreeID);
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constant.mCurrentDocumentFileExplore = null;
        unregisterReceiver(usbReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Constant.Activity = 1;
        initBroadcast();
        if(mActionMode != null)
            mActionMode.finish();
        if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
            doLoad(mPath);
        }else if(mMode == Constant.STORAGEMODE_OTG){
            if (mInitOtgLoad) {
                doLoadOTG(mFile, true);
                mInitOtgLoad = false;
            } else
                doLoadOTG(null, false);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isOnTop()) {
            if (mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD) {
                String parent = new File(mPath).getParent();
                doLoad(parent);
            }else if (mMode == Constant.STORAGEMODE_OTG){
                mCurrentDocumentFile = mCurrentDocumentFile.getParentFile();
                doLoadOTG(null, false);
            }
        }else{
            Constant.mCurrentDocumentFileExplore = null;
            this.finish();
        }

    }

    public void updateLayout(int mode) {
        int count = calculateColumnCount(mode);
        if (mLayout != null) {
            mLayout.setSpanCount(count);
        }
        mIconHelper.setViewMode(mode);
        mRecyclerView.requestLayout();
        LocalPreferences.setBrowserViewMode(this, BrowserFragment.LIST_TYPE_FOLDER, mode);//same as tab type 5
    }

    private boolean isOnTop(){
        if (mMode == Constant.STORAGEMODE_LOCAL)
            return mPath.equals(Constant.ROOT_LOCAL);
        else if (mMode == Constant.STORAGEMODE_SD)
            return mPath.equals(FileFactory.getOuterStoragePath(this, Constant.sd_key_path));
        else if (mMode == Constant.STORAGEMODE_OTG){
            return mCurrentDocumentFile.getParentFile() == null;
        }
        return true;
    }

    private void doRefresh(){
        if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
            doLoad(mPath);
        }else if(mMode == Constant.STORAGEMODE_OTG){
            doLoadOTG(null, false);
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if(reqCode == mSDDocumentTreeID && resCode == RESULT_OK){
            Uri uriTree = data.getData();
            if(checkSD(uriTree)){
                doAction();
            }
        }else if(reqCode == DestinationActivity.REQUEST_CODE && resCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            int actionId = bundle.getInt("actionId");
            String destinationPath = bundle.getString("path");
            Constant.MODE actionMode = (Constant.MODE) bundle.getSerializable("destinationMode");
            ArrayList<DocumentFile> destinationDFiles = Constant.destinationDFile;
            doDestinationAction(actionId, destinationPath, actionMode, destinationDFiles);
        }
    }

    private boolean checkSD(Uri uri){
        if (!uri.toString().contains("primary")) {
            if (uri != null) {
                if(uri.getPath().toString().split(":").length > 1){
                    snackBarShow(R.string.snackbar_plz_select_top);
                }else{
                    rootDir = DocumentFile.fromTreeUri(this, uri);//sd root path
                    ArrayList<String> sdCardFileName = FileInfo.getSDCardFileName(FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path));
                    boolean bSDCard = FileFactory.getInstance().doFileNameCompare(rootDir.listFiles(), sdCardFileName);
                    if(bSDCard){
                        getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        String uid = FileFactory.getSDCardUniqueId();
                        LocalPreferences.setSDKey(this, uid, uri.toString());
                        Constant.mCurrentDocumentFileExplore = Constant.mSDCurrentDocumentFile = Constant.mSDRootDocumentFile = rootDir;
                        return true;
                    }else{
                        snackBarShow(R.string.snackbar_plz_select_sd);
                    }
                }
            }

        }else {
            snackBarShow(R.string.snackbar_plz_select_sd);
        }
        return false;
    }

    private void doAction(){
        switch (nowAction){
            case R.id.menu_new_folder:
                String sd_Path = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
                FileInfo tmpFile = new FileInfo();
                tmpFile.path = ActionParameter.path;
                ArrayList<FileInfo> tmpFileList = new ArrayList<>();
                tmpFileList.add(tmpFile);
                ActionParameter.dFiles = FileFactory.findDocumentFilefromPathSD(tmpFileList, sd_Path, Constant.Activity);
                mFileActionManager.newFolderOTG(ActionParameter.name, ActionParameter.dFiles);
                break;
            case R.id.action_delete:
                String sdPathDelete = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
                ActionParameter.dFiles = FileFactory.findDocumentFilefromPathSD(ActionParameter.files, sdPathDelete, Constant.Activity);
                mFileActionManager.deleteOTG(ActionParameter.dFiles);
                break;
            case R.id.action_rename:
                String sdPathRename = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
                ActionParameter.dFiles = FileFactory.findDocumentFilefromPathSD(ActionParameter.files, sdPathRename, Constant.Activity);
                mFileActionManager.renameOTG(ActionParameter.name, ActionParameter.dFiles);
                break;
            case R.id.action_copy:
                if(ActionParameter.files.get(0).storagemode == Constant.STORAGEMODE_LOCAL){//Local -> SD
                    doLocalCopyorMovetoOTG(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                }else if(ActionParameter.files.get(0).storagemode == Constant.STORAGEMODE_SD){//SD -> SD
                    doSDMoveOrCopytoSD(ActionParameter.files, ActionParameter.path, true);
                }else if(ActionParameter.files.get(0).storagemode == Constant.STORAGEMODE_OTG){//OTG -> SD
                    doOTGCopyorMove(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                }
                break;
            case R.id.action_move:
                if(ActionParameter.files.get(0).storagemode == Constant.STORAGEMODE_LOCAL){//Local -> SD
                    doLocalCopyorMovetoOTG(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                }else if(ActionParameter.files.get(0).storagemode == Constant.STORAGEMODE_SD){//SD -> SD
                    String sdPath = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
                    if (ActionParameter.path.startsWith(Constant.ROOT_LOCAL)) {//SD -> Local
                        doOTGCopyorMovetoLocal(nowAction, ActionParameter.files, ActionParameter.path, true);
                    } else if (sdPath != null && ActionParameter.path.startsWith(sdPath)) {//SD -> SD
                        doSDMoveOrCopytoSD(ActionParameter.files, ActionParameter.path, false);
                    } else {//SD -> OTG
                        doSDMovetoOTG(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                    }
                }else if(ActionParameter.files.get(0).storagemode == Constant.STORAGEMODE_OTG){//OTG -> SD
                    doOTGCopyorMove(nowAction, ActionParameter.files, ActionParameter.dFiles, ActionParameter.path, true);
                }
                break;
            case R.id.action_encrypt:
                doSDEncryptNewFolder();
            case R.id.dialog_decrypt_name:
                String sdPathDecrypt = FileFactory.getOuterStoragePath(this, Constant.sd_key_path);
                ArrayList<DocumentFile> mSelectedDFiles = FileFactory.findDocumentFilefromPathSD(DecryptUtils.getSelectedFileList(), sdPathDecrypt, Constant.Activity);
                DecryptUtils.setSelectedDocumentFile(mSelectedDFiles);
                doSDDecryptNewFolder();
                break;
            default:
                break;
        }
    }

    private void doDestinationAction(int actionId, String destinationPath, Constant.MODE actionMode, ArrayList<DocumentFile> destinationDFiles){
        Constant.Activity = 1;
        ArrayList<FileInfo> mSelectedFiles = mFolderExploreAdapter.getSelectedFiles();
        int source_storage = mSelectedFiles.get(0).storagemode;
        if(actionMode == Constant.MODE.LOCAL){
            if(source_storage == Constant.STORAGEMODE_LOCAL){//Local -> Local
                doLocalCopyorMove(actionId, mSelectedFiles, destinationPath);
            }else if(source_storage == Constant.STORAGEMODE_SD){//SD -> Local
                if(actionId == R.id.action_copy)
                    doLocalCopyorMove(actionId, mSelectedFiles, destinationPath);//copy doesnt need permission
                else {
                    if(checkSDWritePermission()){
                        doOTGCopyorMovetoLocal(actionId, mSelectedFiles, destinationPath, true);
                    }else {
                        nowAction = actionId;
                        ActionParameter.path = destinationPath;
                        ActionParameter.files = mSelectedFiles;
                        ActionParameter.dFiles = destinationDFiles;
                    }
                }
            }else if(source_storage == Constant.STORAGEMODE_OTG){//OTG -> Local
                doOTGCopyorMovetoLocal(actionId, mSelectedFiles, destinationPath, false);
            }
        }else if(actionMode == Constant.MODE.SD){
            if(source_storage == Constant.STORAGEMODE_LOCAL){//Local -> SD
                if(checkSDWritePermission()){
                    doLocalCopyorMovetoOTG(actionId, mSelectedFiles, destinationDFiles, destinationPath, true);
                }else {
                    nowAction = actionId;
                    ActionParameter.path = destinationPath;
                    ActionParameter.files = mSelectedFiles;
                    ActionParameter.dFiles = destinationDFiles;
                }
            }else if(source_storage == Constant.STORAGEMODE_SD){//SD -> SD
                if (checkSDWritePermission()) {
                    if (actionId == R.id.action_copy)
                        doSDMoveOrCopytoSD(mSelectedFiles, destinationPath, true);
                    else
                        doSDMoveOrCopytoSD(mSelectedFiles, destinationPath, false);
                } else {
                    nowAction = actionId;
                    ActionParameter.path = destinationPath;
                    ActionParameter.files = mSelectedFiles;
                    ActionParameter.dFiles = destinationDFiles;
                }
            }else if(source_storage == Constant.STORAGEMODE_OTG){//OTG -> SD
                if(checkSDWritePermission()){
                    doOTGCopyorMove(actionId, mSelectedFiles, destinationDFiles, destinationPath, true);
                }else {
                    nowAction = actionId;
                    ActionParameter.path = destinationPath;
                    ActionParameter.files = mSelectedFiles;
                    ActionParameter.dFiles = destinationDFiles;
                }
            }
        }else if(actionMode == Constant.MODE.OTG){
            if(source_storage == Constant.STORAGEMODE_LOCAL){//Local -> OTG
                doLocalCopyorMovetoOTG(actionId, mSelectedFiles, destinationDFiles, destinationPath, false);
            }else if(source_storage == Constant.STORAGEMODE_SD){//SD -> OTG
                if(actionId == R.id.action_copy)
                    doLocalCopyorMovetoOTG(actionId, mSelectedFiles, destinationDFiles, destinationPath, false);
                else{
                    if(checkSDWritePermission()){
                        doSDMovetoOTG(actionId, mSelectedFiles, destinationDFiles, destinationPath, true);
                    }else{
                        nowAction = actionId;
                        ActionParameter.path = destinationPath;
                        ActionParameter.files = mSelectedFiles;
                        ActionParameter.dFiles = destinationDFiles;
                    }
                }
            }else if(source_storage == Constant.STORAGEMODE_OTG){//OTG -> OTG
                doOTGCopyorMove(actionId, mSelectedFiles, destinationDFiles, destinationPath, false);
            }
        }
    }

    private ArrayList<FileInfo> createListFileInfoFromPath(String path){
        ArrayList<FileInfo> tmpDesFiles = new ArrayList<>();
        FileInfo file = new FileInfo();
        file.path = path;
        tmpDesFiles.add(file);
        return tmpDesFiles;
    }



    @Override
    public void onDropdownItemSelected(int position) {
        if (position > 0) {
            if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
                String path = mDropdownAdapter.getPath(mMode, position);
                doLoad(path);
            }else if(mMode == Constant.STORAGEMODE_OTG){
                String path = mDropdownAdapter.getPath(mMode, position);
                DocumentFile mapDFile = dropDownMapOTG.get(path);
                mCurrentDocumentFile = mapDFile;
                doLoadOTG(null, false);
            }
        }
    }

    @Override
    public void onRecyclerItemClick(int position) {
        int type = mFileList.get(position).type;
        FileInfo file = mFileList.get(position);
        if(mActionMode == null){
            switch (type){
                case Constant.TYPE_DIR:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD)
                        doLoad(mFileList.get(position).path);
                    else if (mMode == Constant.STORAGEMODE_OTG){
                        mCurrentDocumentFile = mCurrentDocumentFile.findFile(mFileList.get(position).name);
                        doLoadOTG(mFileList.get(position), false);
                    }
                    break;
                case Constant.TYPE_PHOTO:
                    startPhotoSingleView(mFileList, position);
                    break;
                case Constant.TYPE_VIDEO:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
                        MediaUtils.execute(this, file.path, getResources().getString(R.string.openin_title));
                    }else if (mMode == Constant.STORAGEMODE_OTG){
                        MediaUtils.executeUri(this, file.uri.toString(), getResources().getString(R.string.openin_title));
                    }
                    break;
                case Constant.TYPE_MUSIC:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
                        MediaUtils.execute(this, file.path, getResources().getString(R.string.openin_title));
                    }else if (mMode == Constant.STORAGEMODE_OTG){
                        MediaUtils.executeUri(this, file.uri.toString(), getResources().getString(R.string.openin_title));
                    }
                    break;
                case Constant.TYPE_DOC:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
                        MediaUtils.execute(this, file.path, getResources().getString(R.string.openin_title));
                    }else if (mMode == Constant.STORAGEMODE_OTG){
                        MediaUtils.executeUri(this, file.uri.toString(), getResources().getString(R.string.openin_title));
                    }
                    break;
                case Constant.TYPE_ENCRYPT:
                    if(mMode == Constant.STORAGEMODE_LOCAL){
                        doLocalDecryptDialog(file);
                    }else if (mMode == Constant.STORAGEMODE_OTG){
                        doOTGDecryptDialog(file);
                    }else if (mMode == Constant.STORAGEMODE_SD){
                        nowAction = R.id.dialog_decrypt_name;
                        doSDDecryptDialog(file);
                    }
                    break;
                case Constant.TYPE_OTHER_FILE:
                    if(mMode == Constant.STORAGEMODE_LOCAL || mMode == Constant.STORAGEMODE_SD){
                        MediaUtils.execute(this, file.path, getResources().getString(R.string.openin_title));
                    }else if (mMode == Constant.STORAGEMODE_OTG){
                        MediaUtils.executeUri(this, file.uri.toString(), getResources().getString(R.string.openin_title));
                    }
                    break;
            }
        }else{
            selectAtPosition(position);
        }

    }

    @Override
    public void onRecyclerItemLongClick(int position) {
        if (mActionMode == null) {
            startActionMode();
            selectAtPosition(position);
        }
    }

    @Override
    public void onRecyclerItemInfoClick(int position) {
        createInfoDialog(this, mFileList.get(position), MainActivity.mScreenW);
    }

    private void startActionMode() {
        if (mActionMode == null)
            startSupportActionMode(this);
    }

    private void selectAtPosition(int position) {
        boolean checked = mFileList.get(position).checked;
        mFileList.get(position).checked = !checked;
        mFolderExploreAdapter.notifyItemChanged(position);
        int count = mFolderExploreAdapter.getItemSelectedCount();
        boolean selectAll = (count == mFileList.size());
        updateActionModeTitle(count);
        toggleActionModeAction(count);
        toggleFabSelectAll(selectAll);
    }

    private void toggleSelectAll() {
        boolean selectAll = mFolderExploreAdapter.getSelectedAllorNot();
        if (selectAll)
            mFolderExploreAdapter.clearAllSelection();
        else
            mFolderExploreAdapter.setAllSelection();
        selectAll = !selectAll;
        updateActionModeTitle(mFolderExploreAdapter.getItemSelectedCount());
        toggleActionModeAction(mFolderExploreAdapter.getItemSelectedCount());
        toggleFabSelectAll(selectAll);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        initView(mode);
        initMenu(menu);
        updateActionModeTitle(0);
        toggleActionModeAction(0);
        toggleFabSelectAll(false);
        return true;
    }

    public void updateActionModeTitle(int count) {
        String format = getResources().getString(R.string.conj_selected);
        mActionModeTitle.setText(String.format(format, count));
    }

    private void toggleFabSelectAll(boolean selectAll) {
        int resId = selectAll
                ? R.mipmap.ic_floating_browser_cancelselect
                : R.mipmap.ic_floating_browser_selectall;
        mFab.setImageResource(resId);
        mFab.setVisibility(View.VISIBLE);
    }

    private void toggleActionModeAction(int count) {
        boolean visible = false;
        if (count == 0) {
            mActionMode.getMenu().findItem(R.id.action_rename).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_share).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_copy).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_move).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_delete).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_encrypt).setVisible(visible);
        } else if (count == 1) {
            mActionMode.getMenu().findItem(R.id.action_rename).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_share).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_copy).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_move).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_delete).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_encrypt).setVisible(!visible);
        } else if (count > 1) {
            mActionMode.getMenu().findItem(R.id.action_rename).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_share).setVisible(visible);
            mActionMode.getMenu().findItem(R.id.action_copy).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_move).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_delete).setVisible(!visible);
            mActionMode.getMenu().findItem(R.id.action_encrypt).setVisible(!visible);
        }
    }

    private void initView(ActionMode mode) {
        mActionMode = mode;
        mActionMode.setCustomView(mActionModeView);
    }

    private void initMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fab_editor, menu);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        FileInfo fileInfo = mFolderExploreAdapter.getSelectedFiles().get(0);
        switch (item.getItemId()) {
            case R.id.action_rename:
                if (fileInfo.storagemode == Constant.STORAGEMODE_LOCAL) {
                    doLocalRename();
                }else if(fileInfo.storagemode == Constant.STORAGEMODE_SD) {
                    nowAction = R.id.action_rename;
                    doOTGRename(true);
                }else if (fileInfo.storagemode == Constant.STORAGEMODE_OTG) {
                    doOTGRename(false);
                }
                break;
            case R.id.action_delete:
                if (fileInfo.storagemode == Constant.STORAGEMODE_LOCAL) {
                    doLocalDelete();
                }else if(fileInfo.storagemode == Constant.STORAGEMODE_SD){
                    nowAction = R.id.action_delete;
                    doOTGDelete(true);
                }else if(fileInfo.storagemode == Constant.STORAGEMODE_OTG){
                    doOTGDelete(false);
                }
                break;
            case R.id.action_share:
                if(fileInfo.storagemode == Constant.STORAGEMODE_LOCAL || fileInfo.storagemode == Constant.STORAGEMODE_SD){
                    doLocalShare();
                }else if(fileInfo.storagemode == Constant.STORAGEMODE_OTG){
                    doOTGShare();
                }
                break;
            case R.id.action_copy:
                startDestinationActivity(R.id.action_copy);
                break;
            case R.id.action_move:
                startDestinationActivity(R.id.action_move);
                break;
            case R.id.action_encrypt:
                if(fileInfo.storagemode == Constant.STORAGEMODE_LOCAL){
                    doLocalEncryptDialog();
                }else if(fileInfo.storagemode == Constant.STORAGEMODE_OTG){
                    doOTGEncryptDialog();
                }else if(fileInfo.storagemode == Constant.STORAGEMODE_SD){
                    nowAction = R.id.action_encrypt;
                    doSDEncryptDialog();
                }
                break;
        }
        return false;
    }

    public void startDestinationActivity(int actionId){
        Intent intent = new Intent();
        Bundle args = new Bundle();
        args.putInt("actionId", actionId);
        intent.putExtras(args);
        intent.setClass(FolderExploreActivity.this, DestinationActivity.class);
        startActivityForResult(intent, DestinationActivity.REQUEST_CODE);
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mFolderExploreAdapter.clearAllSelection();
        mFab.setImageResource(R.mipmap.ic_floating_browser_intoaction);
        mActionMode = null;
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        //loadingContainer.setVisibility(View.VISIBLE);
        Loader<Boolean> loader = mFileActionManager.onCreateLoader(id, args);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
        mFileActionManager.onLoadFinished(loader, success);
        if (success) {
            if (loader instanceof LocalListLoader) {
                mPath = ((LocalListLoader) loader).getPath();
                mFileList = ((LocalListLoader) loader).getFileList();
                updateScreen();
            }else if (loader instanceof OTGFileLoader){
                mPath = ((OTGFileLoader) loader).getPath();
                mFileList = ((OTGFileLoader) loader).getFileList();
                Constant.mCurrentDocumentFileExplore = mCurrentDocumentFile = ((OTGFileLoader)loader).getCurrentDocumentFile();
                updateScreen();
            }else if (loader instanceof LocalEncryptNewFolderLoader) {
                doLocalEncryptCopy();
            } else if (loader instanceof LocalEncryptCopyLoader){
                doLocalEncrypt();
            }else if(loader instanceof OTGEncryptNewFolderLoader){
                doOTGEncryptCopy();
            }else if(loader instanceof OTGCopytoLocalEncryptLoader){
                doOTGEncrypt();
            }else if(loader instanceof OTGEncryptLoader){
                doLocalCopytoOTGEncrypt(false);
            }else if(loader instanceof OTGDecryptNewFolderLoader){
                doOTGDecryptCopy();
            }else if(loader instanceof OTGCopytoLocalDecryptLoader){
                doOTGDecrypt();
            }else if(loader instanceof OTGDecryptLoader){
                doLocalCopytoOTGDecrypt(false);
            }else if(loader instanceof SDEncryptNewFolderLoader){
                doSDEncryptCopy();
            }else if(loader instanceof SDEncryptCopyLoader){
                doSDEncrypt();
            }else if(loader instanceof SDEncryptLoader){
                doLocalCopytoOTGEncrypt(true);
            }else if(loader instanceof SDDecryptNewFolderLoader){
                doSDDecryptCopy();
            }else if(loader instanceof SDCopytoLocalDecryptLoader){
                doSDDecrypt();
            }else if(loader instanceof SDDecryptLoader){
                doLocalCopytoOTGDecrypt(true);
            }else{
                doRefresh();
            }
            if(mActionMode != null){
                mActionMode.finish();
            }

        }
        mSwipeRefreshLayout.setRefreshing(false);
        if(loadingContainer != null)
            loadingContainer.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

    private void doOTGEncryptDialog() {
        //ArrayList<String> names = new ArrayList<String>();
        ArrayList<FileInfo> selectedFiles = mFolderExploreAdapter.getSelectedFiles();
        EncryptUtils.clearAllEncryptUtils();
        new OTGEncryptDialog(this, selectedFiles) {
            @Override
            public void onConfirm(String newName, String password, ArrayList<DocumentFile> mSelectedDFiles) {
                DocumentFile child = mSelectedDFiles.get(0).getParentFile();
                EncryptUtils.setAfterEncryptDFile(child);
                EncryptUtils.setSelectedDocumentFile(mSelectedDFiles);
                EncryptUtils.setEncryptFileName(newName);
                EncryptUtils.setPassword(password);
                doOTGEncryptNewFolder();
                if(mActionMode != null)
                    mActionMode.finish();
            }
        };
    }

    private void doOTGEncryptNewFolder(){
        calendar = Calendar.getInstance();
        String currentDateTimeString = sdf.format(calendar.getTime());
        String folderName = Constant.ROOT_CACHE + File.separator + currentDateTimeString;
        EncryptUtils.setBeforeEncryptPath(folderName);
        mFileActionManager.newFolderEncryptOTG(folderName);
    }

    private void doOTGEncryptCopy(){
        ArrayList<DocumentFile> selectedDFiles = EncryptUtils.getSelectedDocumentFile();
        mFileActionManager.copyOTGtoLocalEncrypt(selectedDFiles, EncryptUtils.getBeforeEncryptPath());
    }

    private void doOTGEncrypt(){
        String password = EncryptUtils.getPassword();
        String beforeEncryptPath = EncryptUtils.getBeforeEncryptPath();
        String afterEncryptPath = EncryptUtils.getBeforeEncryptPath() + File.separator + EncryptUtils.getEncryptFileName();
        EncryptUtils.setAfterEncryptPath(afterEncryptPath);
        ArrayList<String> encryptList = new ArrayList<>();
        encryptList.add(beforeEncryptPath);
        encryptList.add(afterEncryptPath);
        encryptList.add(password);
        mFileActionManager.encryptOTG(encryptList);
    }

    private void doOTGDecryptDialog(FileInfo clickFile){
        ArrayList<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = mFolderExploreAdapter.getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        ArrayList<FileInfo> selectedFile = new ArrayList<>();
        selectedFile.add(clickFile);
        DecryptUtils.clearAllDecryptUtils();
        new OTGDecryptDialog(this, folderNames, selectedFile){
            @Override
            public void onConfirm(String newFolderName, String password, ArrayList<DocumentFile> selectedDFiles) {
                DocumentFile child = selectedDFiles.get(0).getParentFile();
                DecryptUtils.setAfterDecryptDFile(child);
                DecryptUtils.setSelectedDocumentFile(selectedDFiles);
                DecryptUtils.setDecryptFileName(newFolderName);
                DecryptUtils.setPassword(password);
                doOTGDecryptNewFolder();
            }
        };
    }

    private void doOTGDecryptNewFolder(){
        calendar = Calendar.getInstance();
        String currentDateTimeString = sdf.format(calendar.getTime());
        String folderName = Constant.ROOT_CACHE + File.separator + currentDateTimeString;
        DecryptUtils.setBeforeDecryptPath(folderName);
        mFileActionManager.newFolderDecryptOTG(folderName);
    }

    private void doOTGDecryptCopy(){
        ArrayList<DocumentFile> selectedDFiles = DecryptUtils.getSelectedDocumentFile();
        mFileActionManager.copyOTGtoLocalDecrypt(selectedDFiles, DecryptUtils.getBeforeDecryptPath());
    }

    private void doOTGDecrypt(){
        String password = DecryptUtils.getPassword();
        String beforeEncryptPath = DecryptUtils.getBeforeDecryptPath() + File.separator + DecryptUtils.getSelectedDocumentFile().get(0).getName();
        String afterDecryptPath = DecryptUtils.getBeforeDecryptPath() + File.separator + DecryptUtils.getDecryptFileName();
        DecryptUtils.setAfterDecryptPath(afterDecryptPath);
        ArrayList<String> decryptList = new ArrayList<>();
        decryptList.add(afterDecryptPath);
        decryptList.add(password);
        decryptList.add(beforeEncryptPath);
        mFileActionManager.decryptOTG(decryptList);
    }

    private void doLocalCopytoOTGDecrypt(boolean isSrcSDCard){
        if(isSrcSDCard){
            String uid = FileFactory.getSDCardUniqueId();
            String sdKey = LocalPreferences.getSDKey(mContext, uid);
            if(sdKey != ""){
                String getLocalDecryptFilePath = DecryptUtils.getAfterDecryptPath();
                FileInfo tmpFile = new FileInfo();
                tmpFile.path = getLocalDecryptFilePath;
                ArrayList<FileInfo> selectedFiles = new ArrayList<>();
                selectedFiles.add(tmpFile);
                DocumentFile tmpDFile = DecryptUtils.getSelectedDocumentFile().get(0).getParentFile();
                ArrayList<DocumentFile> destinationDFiles = new ArrayList<>();
                destinationDFiles.add(tmpDFile);
                mFileActionManager.copyFromLocaltoOTGDecrypt(selectedFiles, destinationDFiles);
            }
        }else {
            String getLocalDecryptFilePath = DecryptUtils.getAfterDecryptPath();
            FileInfo tmpFile = new FileInfo();
            tmpFile.path = getLocalDecryptFilePath;
            ArrayList<FileInfo> selectedFiles = new ArrayList<>();
            selectedFiles.add(tmpFile);
            DocumentFile tmpDFile = DecryptUtils.getAfterDecryptDFile();
            if(tmpDFile == null)
                tmpDFile = Constant.mRootDocumentFile;
            ArrayList<DocumentFile> destinationDFiles = new ArrayList<>();
            destinationDFiles.add(tmpDFile);
            mFileActionManager.copyFromLocaltoOTGDecrypt(selectedFiles, destinationDFiles);
        }
    }

    private void doLocalCopytoOTGEncrypt(boolean isSrcSDCard){
        if(isSrcSDCard){
            String uid = FileFactory.getSDCardUniqueId();
            String sdKey = LocalPreferences.getSDKey(mContext, uid);
            if(sdKey != ""){
                Uri uriSDKey = Uri.parse(sdKey);
                DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
                Constant.mCurrentDocumentFileExplore = tmpDFile;
                String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);

                String getLocalEncryptFilePath = EncryptUtils.getAfterEncryptPath();
                FileInfo tmpFile = new FileInfo();
                tmpFile.path = getLocalEncryptFilePath + getResources().getString(R.string.encrypt_subfilename);
                ArrayList<FileInfo> selectedFiles = new ArrayList<>();
                selectedFiles.add(tmpFile);
                String copyToSDPath = EncryptUtils.getCopyToSDPath() != "" ? EncryptUtils.getCopyToSDPath() : sdPath;
                ArrayList<FileInfo> files = createListFileInfoFromPath(copyToSDPath);
                ArrayList<DocumentFile> destinationDFiles = FileFactory.findDocumentFilefromPathSD(files, sdPath, Constant.Activity);
                mFileActionManager.copyFromLocaltoOTGEncrypt(selectedFiles, destinationDFiles, copyToSDPath);
            }
        }else {
            String getLocalEncryptFilePath = EncryptUtils.getAfterEncryptPath();
            FileInfo tmpFile = new FileInfo();
            tmpFile.path = getLocalEncryptFilePath + getResources().getString(R.string.encrypt_subfilename);
            ArrayList<FileInfo> selectedFiles = new ArrayList<>();
            selectedFiles.add(tmpFile);
            DocumentFile tmpDFile = EncryptUtils.getAfterEncryptDFile();
            if(tmpDFile == null)
                tmpDFile = Constant.mRootDocumentFile;
            ArrayList<DocumentFile> destinationDFiles = new ArrayList<>();
            destinationDFiles.add(tmpDFile);
            mFileActionManager.copyFromLocaltoOTGEncrypt(selectedFiles, destinationDFiles, "");
        }
    }

    private void doSDEncryptDialog(){
        EncryptUtils.clearAllEncryptUtils();
        new LocalEncryptDialog(this) {
            @Override
            public void onConfirm(String newName, String password) {
                ArrayList<FileInfo> selectedFiles = mFolderExploreAdapter.getSelectedFiles();
                File child = new File(selectedFiles.get(0).path);
                EncryptUtils.setCopyToSDPath(child.getParent());
                EncryptUtils.setSelectLocalFile(selectedFiles);
                EncryptUtils.setEncryptFileName(newName);
                EncryptUtils.setPassword(password);
                if(checkSDWritePermission()){
                    doSDEncryptNewFolder();
                }

                if(mActionMode != null)
                    mActionMode.finish();
            }
        };
    }

    private void doSDEncryptNewFolder(){
        calendar = Calendar.getInstance();
        String currentDateTimeString = sdf.format(calendar.getTime());
        String folderName = Constant.ROOT_CACHE + File.separator + currentDateTimeString;
        EncryptUtils.setBeforeEncryptPath(folderName);
        mFileActionManager.newFolderEncryptSD(folderName);
    }

    private void doSDEncryptCopy(){
        ArrayList<FileInfo> selectedFiles = EncryptUtils.getSelectLocalFile();
        mFileActionManager.copyEncryptSD(selectedFiles, EncryptUtils.getBeforeEncryptPath());
    }

    private void doSDEncrypt(){
        String password = EncryptUtils.getPassword();
        String beforeEncryptPath = EncryptUtils.getBeforeEncryptPath();
        String afterEncryptPath = EncryptUtils.getBeforeEncryptPath() + File.separator + EncryptUtils.getEncryptFileName();
        EncryptUtils.setAfterEncryptPath(afterEncryptPath);
        ArrayList<String> encryptList = new ArrayList<>();
        encryptList.add(beforeEncryptPath);
        encryptList.add(afterEncryptPath);
        encryptList.add(password);
        mFileActionManager.encryptSD(encryptList);
    }

    private void doSDDecryptDialog(FileInfo clickFile){
        DecryptUtils.clearAllDecryptUtils();
        ArrayList<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = mFolderExploreAdapter.getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        ArrayList<FileInfo> selectedFile = new ArrayList<>();
        selectedFile.add(clickFile);
        DecryptUtils.setSelectedFileList(selectedFile);
        new SDDecryptDialog(this, folderNames){
            @Override
            public void onConfirm(String newFolderName, String password) {
                if(checkSDWritePermission()){
                    String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                    ArrayList<DocumentFile> mSelectedDFiles = FileFactory.findDocumentFilefromPathSD(DecryptUtils.getSelectedFileList(), sdPath, Constant.Activity);
                    DocumentFile child = mSelectedDFiles.get(0).getParentFile();
                    DecryptUtils.setAfterDecryptDFile(child);
                    DecryptUtils.setSelectedDocumentFile(mSelectedDFiles);
                    DecryptUtils.setDecryptFileName(newFolderName);
                    DecryptUtils.setPassword(password);
                    doSDDecryptNewFolder();
                }else {
                    DecryptUtils.setDecryptFileName(newFolderName);
                    DecryptUtils.setPassword(password);
                }
            }
        };
    }

    private void doSDDecryptNewFolder(){
        calendar = Calendar.getInstance();
        String currentDateTimeString = sdf.format(calendar.getTime());
        String folderName = Constant.ROOT_CACHE + File.separator + currentDateTimeString;
        DecryptUtils.setBeforeDecryptPath(folderName);
        mFileActionManager.newFolderDecryptSD(folderName);
    }

    private void doSDDecryptCopy(){
        ArrayList<DocumentFile> selectedDFiles = DecryptUtils.getSelectedDocumentFile();
        mFileActionManager.copySDtoLocalDecrypt(selectedDFiles, DecryptUtils.getBeforeDecryptPath());
    }

    private void doSDDecrypt(){
        String password = DecryptUtils.getPassword();
        String beforeEncryptPath = DecryptUtils.getBeforeDecryptPath() + File.separator + DecryptUtils.getSelectedDocumentFile().get(0).getName();
        String afterDecryptPath = DecryptUtils.getBeforeDecryptPath() + File.separator + DecryptUtils.getDecryptFileName();
        DecryptUtils.setAfterDecryptPath(afterDecryptPath);
        ArrayList<String> decryptList = new ArrayList<>();
        decryptList.add(afterDecryptPath);
        decryptList.add(password);
        decryptList.add(beforeEncryptPath);
        mFileActionManager.decryptSD(decryptList);
    }

    private void doLocalEncryptDialog() {
        EncryptUtils.clearAllEncryptUtils();
        new LocalEncryptDialog(this) {
            @Override
            public void onConfirm(String newName, String password) {
                ArrayList<FileInfo> selectedFiles = mFolderExploreAdapter.getSelectedFiles();
                File child = new File(selectedFiles.get(0).path);
                EncryptUtils.setAfterEncryptPath(child.getParent() + File.separator + newName);
                EncryptUtils.setSelectLocalFile(selectedFiles);
                EncryptUtils.setEncryptFileName(newName);
                EncryptUtils.setPassword(password);
                doLocalEncryptNewFolder();
                if(mActionMode != null)
                    mActionMode.finish();
            }
        };
    }

    private void doLocalEncryptNewFolder(){
        calendar = Calendar.getInstance();
        String currentDateTimeString = sdf.format(calendar.getTime());
        String folderName = Constant.ROOT_CACHE + File.separator + currentDateTimeString;
        EncryptUtils.setBeforeEncryptPath(folderName);
        mFileActionManager.newFolderEncrypt(folderName);
    }

    private void doLocalEncryptCopy(){
        ArrayList<FileInfo> selectedFiles = EncryptUtils.getSelectLocalFile();
        mFileActionManager.copyEncrypt(selectedFiles, EncryptUtils.getBeforeEncryptPath());
    }

    private void doLocalEncrypt(){
        String password = EncryptUtils.getPassword();
        String beforeEncryptPath = EncryptUtils.getBeforeEncryptPath();
        String afterEncryptPath = EncryptUtils.getAfterEncryptPath();
        if(afterEncryptPath.equals("")||afterEncryptPath.equals(null))
            afterEncryptPath = Constant.ROOT_LOCAL + File.separator + EncryptUtils.getEncryptFileName();
        ArrayList<String> encryptList = new ArrayList<>();
        encryptList.add(beforeEncryptPath);
        encryptList.add(afterEncryptPath);
        encryptList.add(password);
        mFileActionManager.encrypt(encryptList);
    }

    private void doLocalDecryptDialog(FileInfo selectedfile){
        ArrayList<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = mFolderExploreAdapter.getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        DecryptUtils.clearAllDecryptUtils();
        new LocalDecryptDialog(this, folderNames, selectedfile.path) {
            @Override
            public void onConfirm(String newFolderpath, String password, String filePath) {
                doLocalDecrypt(newFolderpath, password, filePath);
            }
        };
    }

    private void doLocalDecrypt(String decryptPath, String password, String encryptPath){
        ArrayList<String> decryptList = new ArrayList<>();
        decryptList.add(decryptPath);
        decryptList.add(password);
        decryptList.add(encryptPath);
        mFileActionManager.decrypt(decryptList);
    }

    private void doLocalNewFolder(){
        List<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = mFolderExploreAdapter.getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        new LocalNewFolderDialog(this, folderNames) {
            @Override
            public void onConfirm(String newName) {
                String path = mPath;
                StringBuilder builder = new StringBuilder(path);
                if (!path.endsWith("/"))
                    builder.append("/");
                builder.append(newName);
                String newFolderPath = builder.toString();
                mFileActionManager.newFolder(newFolderPath);
            }
        };
    }

    private void doOTGNewFolder(final boolean bSDCard) {
        List<String> folderNames = new ArrayList<String>();
        ArrayList<FileInfo> allFiles = mFolderExploreAdapter.getAllFiles();
        for (FileInfo file : allFiles) {
            if (file.type == Constant.TYPE_DIR)
                folderNames.add(file.name.toLowerCase());
        }
        ActionParameter.path = mPath;
        new OTGNewFolderDialog(this, folderNames, Constant.Activity, bSDCard) {
            @Override
            public void onConfirm(String newName, ArrayList<DocumentFile> mDFiles) {
                if(bSDCard){
                    if(checkSDWritePermission()){
                        ActionParameter.name = newName;
                        mFileActionManager.newFolderOTG(newName, mDFiles);
                    }else{
                        ActionParameter.name = newName;
                    }

                }else{
                    mFileActionManager.newFolderOTG(newName, mDFiles);
                }
            }
        };
    }

    private void doLocalRename(){
        FileInfo target = mFolderExploreAdapter.getSelectedFiles().get(0);
        final String path = target.path;
        final String name = target.name;
        boolean ignoreType = (target.type == Constant.TYPE_DIR);
        new LocalRenameDialog(this,ignoreType, name) {
            @Override
            public void onConfirm(String newName) {
                if (newName.equals(name))
                    return;
                mFileActionManager.rename(path, newName);
            }
        };
    }

    private void doOTGRename(final boolean bSDCard) {
        final ArrayList<FileInfo> selectedFiles = mFolderExploreAdapter.getSelectedFiles();
        ActionParameter.files = selectedFiles;
        new OTGRenameDialog(this, selectedFiles, !bSDCard, true) {
            @Override
            public void onConfirm(String newName, String oldName, ArrayList<DocumentFile> selectedDocumentFile) {
                if (newName.equals(oldName))
                    return;
                if(bSDCard){
                    if(checkSDWritePermission()){
                        ActionParameter.name = newName;
                        mFileActionManager.renameOTG(newName, selectedDocumentFile);
                    }else{
                        ActionParameter.name = newName;
                    }
                }else{
                    mFileActionManager.renameOTG(newName, selectedDocumentFile);
                }

            }
        };
    }

    private void doLocalDelete(){
        ArrayList<FileInfo> selectedFiles = mFolderExploreAdapter.getSelectedFiles();
        new LocalDeleteDialog(this, selectedFiles) {
            @Override
            public void onConfirm(ArrayList<FileInfo> selectedFiles) {
                loadingContainer.setVisibility(View.VISIBLE);
                mFileActionManager.delete(selectedFiles);
            }
        };
    }

    private void doOTGDelete(final boolean bSDCard){
        final ArrayList<FileInfo> selectedFiles = mFolderExploreAdapter.getSelectedFiles();
        new OTGDeleteDialog(this, selectedFiles, !bSDCard, true) {
            @Override
            public void onConfirm(ArrayList<DocumentFile> selectedDocumentFile) {
                if(bSDCard){
                    if(checkSDWritePermission()){
                        loadingContainer.setVisibility(View.VISIBLE);
                        mFileActionManager.deleteOTG(selectedDocumentFile);
                    }else{
                        ActionParameter.files = selectedFiles;
                    }
                }else{
                    loadingContainer.setVisibility(View.VISIBLE);
                    mFileActionManager.deleteOTG(selectedDocumentFile);
                }
            }
        };
    }

    private void doLocalShare() {
        String selectPath = mFolderExploreAdapter.getSelectedFiles().get(0).path;
        boolean shareSuccess = MediaUtils.localShare(this, selectPath);
        if(!shareSuccess)
            snackBarShow(R.string.snackbar_not_support_share);
        mActionMode.finish();
    }

    private void doOTGShare(){
        ArrayList<FileInfo> selectFiles = mFolderExploreAdapter.getSelectedFiles();
        ArrayList<DocumentFile> selectDFiles = FileFactory.findDocumentFilefromName(selectFiles, Constant.Activity);
        boolean shareSuccess = MediaUtils.otgShare(this, selectDFiles.get(0));
        if(!shareSuccess)
            snackBarShow(R.string.snackbar_not_support_share);
        mActionMode.finish();
    }

    private void doLocalCopyorMove(int actionId, ArrayList<FileInfo> selectedFiles, String destinationPath) {
        for (FileInfo fileInfo : selectedFiles) {
            if(fileInfo.type == Constant.TYPE_DIR){
                if (destinationPath.startsWith(fileInfo.path)) {
                    snackBarShow(R.string.select_folder_error);
                    return;
                }
            }
        }
        if(actionId == R.id.action_copy)
            mFileActionManager.copy(selectedFiles, destinationPath);
        else if(actionId == R.id.action_move)
            mFileActionManager.move(selectedFiles, destinationPath);
    }

    private void doLocalCopyorMovetoOTG(int actionId, ArrayList<FileInfo> selectedFiles, ArrayList<DocumentFile> destinationDFiles, String destinationPath, boolean isSrcSDCard){
        if(isSrcSDCard){
            String uid = FileFactory.getSDCardUniqueId();
            String sdKey = LocalPreferences.getSDKey(mContext, uid);
            if(sdKey != ""){
                Uri uriSDKey = Uri.parse(sdKey);
                DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
                Constant.mSDCurrentDocumentFile = tmpDFile;
                String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                ArrayList<FileInfo> files = createListFileInfoFromPath(destinationPath);
                ArrayList<DocumentFile> destDFiles = FileFactory.findDocumentFilefromPathSD(files, sdPath, Constant.Activity);
                if(actionId == R.id.action_copy)
                    mFileActionManager.copyFromLocaltoOTG(selectedFiles, destDFiles, destinationPath);
                else if(actionId == R.id.action_move)
                    mFileActionManager.moveFromLocaltoOTG(selectedFiles, destDFiles, destinationPath);
            }
        }else {
            if(actionId == R.id.action_copy)
                mFileActionManager.copyFromLocaltoOTG(selectedFiles, destinationDFiles, "");
            else if(actionId == R.id.action_move)
                mFileActionManager.moveFromLocaltoOTG(selectedFiles, destinationDFiles, "");
        }
    }

    private void doSDMoveOrCopytoSD(ArrayList<FileInfo> selectedFiles, String destinationPath, boolean isCopy) {
        for (FileInfo fileinfo : selectedFiles) {
            new SDMoveToSDTask(mContext, fileinfo, destinationPath, isCopy).execute();
        }
    }

    private void doSDMovetoOTG(int actionId, ArrayList<FileInfo> selectedFiles, ArrayList<DocumentFile> destinationDFiles, String destinationPath, boolean isSrcSDCard){
        String uid = FileFactory.getSDCardUniqueId();
        String sdKey = LocalPreferences.getSDKey(mContext, uid);
        Uri uriSDKey = Uri.parse(sdKey);
        DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
        Constant.mSDCurrentDocumentFile = tmpDFile;
        String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
        ArrayList<FileInfo> files = createListFileInfoFromPath(destinationPath);
        ArrayList<DocumentFile> srcDFiles = FileFactory.findDocumentFilefromPathSD(selectedFiles, sdPath, Constant.Activity);
        mFileActionManager.moveOTG(srcDFiles, destinationDFiles, destinationPath);

    }

    private void doOTGCopyorMove(int actionId, ArrayList<FileInfo> selectedFiles, ArrayList<DocumentFile> destinationDFiles, String destinationPath, boolean isSrcSDCard) {
        if(isSrcSDCard){
            String uid = FileFactory.getSDCardUniqueId();
            String sdKey = LocalPreferences.getSDKey(mContext, uid);
            if(sdKey != ""){
                Uri uriSDKey = Uri.parse(sdKey);
                DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
                Constant.mSDCurrentDocumentFile = tmpDFile;
                String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                ArrayList<FileInfo> files = createListFileInfoFromPath(destinationPath);
                ArrayList<DocumentFile> destDFiles = FileFactory.findDocumentFilefromPathSD(files, sdPath, Constant.Activity);
                String otgPath = FileFactory.getOTGStoragePath(mContext, Constant.otg_key_path);
                ArrayList<DocumentFile> srcDFiles = FileFactory.findDocumentFilefromPathOTG(selectedFiles, otgPath, Constant.Activity);
                if(actionId == R.id.action_copy)
                    mFileActionManager.copyOTG(srcDFiles, destDFiles, destinationPath);
                else if(actionId == R.id.action_move)
                    mFileActionManager.moveOTG(srcDFiles, destDFiles, destinationPath);
            }
        }else{
            String otgPath = FileFactory.getOTGStoragePath(mContext, Constant.otg_key_path);
            ArrayList<DocumentFile> srcDFiles = FileFactory.findDocumentFilefromPathOTG(selectedFiles, otgPath, Constant.Activity);
            for(DocumentFile dFile : srcDFiles){
                if(dFile.isDirectory()){
                    DocumentFile tmpDFile = destinationDFiles.get(0);
                    while (tmpDFile != null){
                        if (dFile.getUri().toString().equals(tmpDFile.getUri().toString())){
                            snackBarShow(R.string.select_folder_error);
                            return;
                        }else{
                            tmpDFile = tmpDFile.getParentFile();
                        }
                    }
                }
            }
            if(actionId == R.id.action_copy)
                mFileActionManager.copyOTG(srcDFiles, destinationDFiles, "");
            else if(actionId == R.id.action_move)
                mFileActionManager.moveOTG(srcDFiles, destinationDFiles, "");

        }
    }

    private void doOTGCopyorMovetoLocal(int actionId, ArrayList<FileInfo> selectedFiles, String destinationPath, boolean isDesSDCard){
        if(isDesSDCard){
            String uid = FileFactory.getSDCardUniqueId();
            String sdKey = LocalPreferences.getSDKey(mContext, uid);
            if(sdKey != ""){
                Uri uriSDKey = Uri.parse(sdKey);
                DocumentFile tmpDFile = DocumentFile.fromTreeUri(mContext, uriSDKey);
                Constant.mCurrentDocumentFileExplore = tmpDFile;
                String sdPath = FileFactory.getOuterStoragePath(mContext, Constant.sd_key_path);
                ArrayList<DocumentFile> srcDFiles = FileFactory.findDocumentFilefromPathSD(selectedFiles, sdPath, Constant.Activity);
                if(actionId == R.id.action_copy)
                    mFileActionManager.copyOTGtoLocal(srcDFiles, destinationPath);
                else if(actionId == R.id.action_move)
                    mFileActionManager.moveOTGtoLocal(srcDFiles, destinationPath);
            }
        }else {
            String otgPath = FileFactory.getOTGStoragePath(mContext, Constant.otg_key_path);
            ArrayList<DocumentFile> srcDFiles = FileFactory.findDocumentFilefromPathOTG(selectedFiles, otgPath, Constant.Activity);
            if(actionId == R.id.action_copy)
                mFileActionManager.copyOTGtoLocal(srcDFiles, destinationPath);
            else if(actionId == R.id.action_move)
                mFileActionManager.moveOTGtoLocal(srcDFiles, destinationPath);
        }
    }

    private int calculateColumnCount(int mode) {
        if (mode == Constant.ITEM_LIST) {
            // List mode is a "grid" with 1 column.
            return 1;
        }
        int cellWidth = getResources().getDimensionPixelSize(R.dimen.grid_width);
        int viewPadding = 0;
        int cellMargin = 0;
        int columnCount = Math.max(2,
                (mScreenW - viewPadding) / (cellWidth + cellMargin));

        return columnCount;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenW = displaymetrics.widthPixels;
        int layout_mode = LocalPreferences.getBrowserViewMode(this, BrowserFragment.LIST_TYPE_FOLDER, Constant.ITEM_LIST);
        updateLayout(layout_mode);
    }

    private final View.OnClickListener mOnSortClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            int sort_by = LocalPreferences.getPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE);
            if (v.getTag().equals("date") && sort_by != Constant.SORT_BY_DATE) {
                v.getRootView().findViewById(R.id.arrow_sort_date).setVisibility(View.VISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_name).setVisibility(View.INVISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_size).setVisibility(View.INVISIBLE);
                LocalPreferences.setPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE);
                Collections.sort(mFileList, FileInfoSort.comparator(mContext));
                FileFactory.getInstance().addFileTypeSortRule(mFileList);
                updateScreen();
            } else if (v.getTag().equals("name") && sort_by != Constant.SORT_BY_NAME) {
                v.getRootView().findViewById(R.id.arrow_sort_date).setVisibility(View.INVISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_name).setVisibility(View.VISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_size).setVisibility(View.INVISIBLE);
                LocalPreferences.setPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_NAME);
                Collections.sort(mFileList, FileInfoSort.comparator(mContext));
                FileFactory.getInstance().addFileTypeSortRule(mFileList);
                updateScreen();
            } else if (v.getTag().equals("size") && sort_by != Constant.SORT_BY_SIZE) {
                v.getRootView().findViewById(R.id.arrow_sort_date).setVisibility(View.INVISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_name).setVisibility(View.INVISIBLE);
                v.getRootView().findViewById(R.id.arrow_sort_size).setVisibility(View.VISIBLE);
                LocalPreferences.setPref(mContext, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_SIZE);
                Collections.sort(mFileList, FileInfoSort.comparator(mContext));
                //don't need this when sort by size
                //FileFactory.getInstance().addFileTypeSortRule(mFileList);
                updateScreen();
            } else {
                boolean sortOrderAsc = LocalPreferences.getPref(mContext,
                        LocalPreferences.BROWSER_SORT_ORDER_PREFIX, Constant.SORT_ORDER_DES) == Constant.SORT_ORDER_AS;
                LocalPreferences.setPref(mContext, LocalPreferences.BROWSER_SORT_ORDER_PREFIX, sortOrderAsc ? Constant.SORT_ORDER_DES : Constant.SORT_ORDER_AS);
                Collections.sort(mFileList, FileInfoSort.comparator(mContext));
                if (!v.getTag().equals("size"))
                    FileFactory.getInstance().addFileTypeSortRule(mFileList);
                updateScreen();
                updateSortArrow(v.getRootView(), mContext);
            }
        }
    };

    private void createPopupWindow(View toolBarView, final Context context) {
        int xy[] = {0,0};
        toolBarView.getLocationOnScreen(xy);

        View layout = getLayoutInflater().inflate(R.layout.easy_sort_layout, null, false);
        PopupWindow easySortView = new PopupWindow(layout, toolBarView.getWidth(), toolBarView.getHeight(), true);
        easySortView.setBackgroundDrawable(new BitmapDrawable());
        easySortView.setOutsideTouchable(true);
        View rootView = ((ViewGroup) (getWindow().getDecorView().findViewById(android.R.id.content))).getChildAt(0);
        easySortView.setAnimationStyle(R.style.PopupWindowAnimation);
        easySortView.showAtLocation(rootView, Gravity.LEFT|Gravity.TOP, xy[0], xy[1]);

        RadioButton b_date = (RadioButton) layout.findViewById(R.id.btn_sort_date);
        RadioButton b_name = (RadioButton) layout.findViewById(R.id.btn_sort_name);
        RadioButton b_size = (RadioButton) layout.findViewById(R.id.btn_sort_size);
        int sort_by = LocalPreferences.getPref(context, LocalPreferences.BROWSER_SORT_PREFIX, Constant.SORT_BY_DATE);
        switch (sort_by) {
            case Constant.SORT_BY_DATE:
                b_date.setChecked(true);
                layout.findViewById(R.id.arrow_sort_date).setVisibility(View.VISIBLE);
                break;
            case Constant.SORT_BY_NAME:
                b_name.setChecked(true);
                layout.findViewById(R.id.arrow_sort_name).setVisibility(View.VISIBLE);
                break;
            case Constant.SORT_BY_SIZE:
                b_size.setChecked(true);
                layout.findViewById(R.id.arrow_sort_size).setVisibility(View.VISIBLE);
                break;
        }
        updateSortArrow(layout, mContext);
        b_date.setOnClickListener(mOnSortClickListener);
        b_name.setOnClickListener(mOnSortClickListener);
        b_size.setOnClickListener(mOnSortClickListener);
    }

    private void updateSortArrow (final View layout, Context context) {
        String arrow = getSortArrow(context);
        ((TextView) layout.findViewById(R.id.arrow_sort_date)).setText(arrow);
        ((TextView) layout.findViewById(R.id.arrow_sort_name)).setText(arrow);
        ((TextView) layout.findViewById(R.id.arrow_sort_size)).setText(arrow);
    }

    private String getSortArrow(Context context) {
        int order = LocalPreferences.getPref(context, LocalPreferences.BROWSER_SORT_ORDER_PREFIX, Constant.SORT_ORDER_DES);
        if (order == Constant.SORT_ORDER_AS) {
            return context.getResources().getString(R.string.top_arrow);
        } else {
            return context.getResources().getString(R.string.bottom_arrow);
        }
    }

    class CustomMenuItemClicked implements MenuItem.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Menu menu = item.getSubMenu();

            final MenuItem grid = menu.findItem(R.id.menu_grid);
            final MenuItem list = menu.findItem(R.id.menu_list);
            int layout_mode = LocalPreferences.getBrowserViewMode(mContext, BrowserFragment.LIST_TYPE_FOLDER, Constant.ITEM_LIST);
            grid.setVisible(layout_mode == Constant.ITEM_LIST);
            list.setVisible(layout_mode == Constant.ITEM_GRID);

            return false;
        }
    }
}

