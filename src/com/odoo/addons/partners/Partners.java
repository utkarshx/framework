package com.odoo.addons.partners;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.OList;
import odoo.controls.OList.OnListBottomReachedListener;
import odoo.controls.OList.OnRowClickListener;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;
import android.widgets.SwipeRefreshLayout;
import android.widgets.SwipeRefreshLayout.OnRefreshListener;

import com.odoo.R;
import com.odoo.addons.partners.providers.partners.PartnersProvider;
import com.odoo.base.res.ResPartner;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.AppScope;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class Partners extends BaseFragment implements OnRowClickListener,
		OnListBottomReachedListener, OnRefreshListener {

	public static final String TAG = Partners.class.getSimpleName();
	public static final String KEY = "partner_type";

	private View mView = null;
	private List<ODataRow> mListRecords = new ArrayList<ODataRow>();
	private OList mListcontrol = null;
	private DataLoader mDataLoader = null;
	private Integer mLimit = 10;
	private Integer mLastPosition = -1;
	private Integer mOffset = 0;
	private SwipeRefreshLayout mSwipeRefresh = null;

	public enum Type {
		Customers, Suppliers, Companies
	}

	private Type mCurrentType = Type.Companies;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(this);
		initArgs();
		mView = inflater.inflate(R.layout.partners_list, container, false);
		init();
		return mView;
	}

	private void initArgs() {
		Bundle bundle = getArguments();
		if (bundle.containsKey(KEY)) {
			mCurrentType = Type.valueOf(bundle.getString(KEY));
		}
	}

	private void init() {
		mSwipeRefresh = (SwipeRefreshLayout) mView
				.findViewById(R.id.swipe_container);
		mSwipeRefresh.setOnRefreshListener(this);
		mSwipeRefresh.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		OControls.setVisible(mView, R.id.loadingProgress);
		mListcontrol = (OList) mView.findViewById(R.id.listRecords);
		mListcontrol.setOnRowClickListener(this);
		mListcontrol.setOnListBottomReachedListener(this);
		mListcontrol.setRecordOffset(mListRecords.size());
		if (mLastPosition == -1) {
			mDataLoader = new DataLoader(mOffset);
			mDataLoader.execute();
		} else {
			showData();
		}
	}

	private void showData() {
		mListcontrol.setCustomView(R.layout.partners_list_item);
		mListcontrol.initListControl(mListRecords);
		OControls.setGone(mView, R.id.loadingProgress);
	}

	@Override
	public Object databaseHelper(Context context) {
		return new ResPartner(context);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.menu_partners, menu);
		SearchView mSearchView = (SearchView) menu.findItem(
				R.id.menu_partner_search).getActionView();
		if (mListcontrol != null)
			mSearchView.setOnQueryTextListener(mListcontrol.getQueryListener());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_partner_create:
			PartnersDetail partner = new PartnersDetail();
			startFragment(partner, true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class DataLoader extends AsyncTask<Void, Void, Void> {
		Integer offset = 0;

		public DataLoader(Integer offset) {
			this.offset = offset;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {

			}
			scope.main().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (db().isEmptyTable()) {
						mSwipeRefresh.setRefreshing(true);
						scope.main().requestSync(PartnersProvider.AUTHORITY);
					}
					if (offset == 0) {
						mListRecords.clear();
					}
					OModel model = db();
					model.setOffset(offset);
					Object[] args = new Object[] { true };
					mListRecords.addAll(model
							.setLimit(mLimit)
							.setOffset(offset)
							.select(getWhere(mCurrentType), args, null, null,
									"local_id DESC"));
					mOffset = model.getNextOffset();
					mListcontrol.setRecordOffset(mOffset);
				}
			});
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			showData();
		}
	}

	private String getWhere(Type type) {
		String where = null;
		switch (type) {
		case Companies:
			where = "is_company = ?";
			break;
		case Customers:
			where = "customer = ?";
			break;
		case Suppliers:
			where = "supplier = ?";
			break;
		}
		return where;

	}

	private int count(Context context, Type type) {
		String where = getWhere(type);
		Object[] args = new Object[] { true };
		return new ResPartner(context).count(where, args);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(TAG, "Companies",
				count(context, Type.Companies), R.drawable.ic_action_company,
				object(Type.Companies)));
		menu.add(new DrawerItem(TAG, "Customers",
				count(context, Type.Customers), R.drawable.ic_action_user,
				object(Type.Customers)));
		menu.add(new DrawerItem(TAG, "Suppliers",
				count(context, Type.Suppliers), R.drawable.ic_action_suppliers,
				object(Type.Suppliers)));
		return menu;
	}

	private Fragment object(Type type) {
		Partners partners = new Partners();
		Bundle args = new Bundle();
		args.putString(KEY, type.toString());
		partners.setArguments(args);
		return partners;
	}

	@Override
	public void onRowItemClick(int position, View view, ODataRow row) {
		mLastPosition = position;
		PartnersDetail partner = new PartnersDetail();
		Bundle arg = new Bundle();
		arg.putAll(row.getPrimaryBundleData());
		partner.setArguments(arg);
		startFragment(partner, true);
	}

	@Override
	public void onResume() {
		super.onResume();
		scope.main().registerReceiver(mSyncFinishReceiver,
				new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
	}

	@Override
	public void onPause() {
		super.onPause();
		scope.main().unregisterReceiver(mSyncFinishReceiver);
	}

	SyncFinishReceiver mSyncFinishReceiver = new SyncFinishReceiver() {
		@Override
		public void onReceive(Context context, android.content.Intent intent) {
			scope.main().refreshDrawer(TAG);
			hideRefreshingProgress();
			if (mDataLoader != null) {
				mDataLoader.cancel(true);
			}
			mDataLoader = new DataLoader(0);
			mDataLoader.execute();
		}
	};

	@Override
	public void onBottomReached(Integer record_limit, Integer record_offset) {
		if (mDataLoader != null) {
			mDataLoader.cancel(true);
		}
		mDataLoader = new DataLoader(record_offset);
		mDataLoader.execute();
	}

	@Override
	public Boolean showLoader() {
		return true;
	}

	@Override
	public void onRefresh() {
		if (app().inNetwork()) {
			scope.main().requestSync(PartnersProvider.AUTHORITY);
		} else {
			hideRefreshingProgress();
			Toast.makeText(getActivity(), "No Connection", Toast.LENGTH_LONG)
					.show();
		}
	}

	private void hideRefreshingProgress() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mSwipeRefresh.setRefreshing(false);
			}
		}, 1000);
	}
}
