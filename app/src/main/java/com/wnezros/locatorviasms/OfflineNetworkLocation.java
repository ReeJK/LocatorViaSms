package com.wnezros.locatorviasms;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OfflineNetworkLocation {
    private final TelephonyManager _tm;
    private final WifiManager _wifi;

    public OfflineNetworkLocation(Context context) {
        _tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        _wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public String getStringData() {
        StringBuilder sb = new StringBuilder();

        List<CellData> cells = getCellInfos();
        if(cells != null) {
            for (CellData c : cells) {
                sb.append("type: ").append(c.Type).append("\n");
                sb.append("cid: ").append(c.CellId).append("\n");
                sb.append("lac: ").append(c.LAC).append("\n");
                sb.append("mcc: ").append(c.MCC).append("\n");
                sb.append("mnc: ").append(c.MNC).append("\n");
                sb.append("dbm: ").append(c.DBM).append("\n");
                sb.append("\n");
            }
        }

        List<WifiData> wifis = getWifiInfos();
        if(wifis != null) {
            for (WifiData w : wifis) {
                sb.append("ssid: ").append(w.BSSID).append("\n");
                sb.append("dbm: ").append(w.DBM).append("\n");
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public String getEncodedData() {
        StringBuilder sb = new StringBuilder();

        List<CellData> cells = getCellInfos();
        if(cells != null && cells.size() > 0) {
            int cidSize = 2;
            switch (cells.get(0).Type) {
                case GSM: sb.append('G'); break;
                case CDMA: sb.append('C'); break;
                case WCDMA: sb.append('W'); cidSize = 4; break;
                case LTE: sb.append('L'); cidSize = 4; break;
                default: sb.append('?'); break;
            }

            for (CellData c : cells) {
                sb.append(intToCode(c.CellId, cidSize));
                sb.append(intToCode(c.LAC, 2));
                sb.append(intToCode(c.MCC, 2));
                sb.append(intToCode(c.MNC, 2));
                sb.append(dBmToCode(c.DBM));
            }
        } else {
            sb.append('-');
        }

        List<WifiData> wifis = getWifiInfos();
        if(wifis != null && wifis.size() > 0) {
            sb.append('W');
            for (WifiData w : wifis) {
                for(int i = 0; i < w.BSSID.length(); i++) {
                    char c = w.BSSID.charAt(i);
                    if(c != ':')
                        sb.append(Character.toUpperCase(c));
                }
                sb.append(dBmToCode(w.DBM));
            }
        }

        return sb.toString();
    }

    private static String dBmToCode(int value) {
        if(value > 40)
            value = 40;
        if(value < -200)
            value = -200;

        return intToCode(Math.abs(value - 40), 1);
    }

    private static String intToCode(int value, int bytes) {
        int size = bytes * 2;
        String hex = Integer.toHexString(value);
        if(hex.length() == size)
            return hex;
        if(hex.length() > size)
            return hex.substring(0, size);

        char[] chars = new char[size - hex.length()];
        Arrays.fill(chars, '0');
        return String.valueOf(chars) + hex;
    }

    private List<CellData> getCellInfos() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            int networkMcc;
            try {
                networkMcc = Integer.parseInt(_tm.getNetworkOperator().substring(0, 3));
            } catch (Exception e) {
                networkMcc = -1;
            }

            List<CellInfo> cells = _tm.getAllCellInfo();
            if(cells == null)
                return null;

            List<CellData> result = new ArrayList<>();
            for(CellInfo cell : cells) {
                if(cell instanceof CellInfoGsm) {
                    CellInfoGsm gsm = (CellInfoGsm) cell;
                    CellIdentityGsm cellId = gsm.getCellIdentity();
                    int cid = cellId.getCid();
                    if(cid > 0xFFFF)
                        continue;

                    int lac = cellId.getLac();
                    int mcc = cellId.getMcc();
                    int mnc = cellId.getMnc();
                    int dbm = gsm.getCellSignalStrength().getDbm();
                    result.add(new CellData(NetworkType.GSM ,cid, lac, mcc, mnc, dbm));
                } else if(cell instanceof CellInfoCdma) {
                    CellInfoCdma cdma = (CellInfoCdma) cell;
                    CellIdentityCdma cellId = cdma.getCellIdentity();
                    int cid = cellId.getBasestationId();
                    if(cid > 0xFFFF)
                        continue;

                    int lac = cellId.getNetworkId();
                    int mcc = networkMcc;
                    int mnc = cellId.getSystemId();
                    int dbm = cdma.getCellSignalStrength().getDbm();
                    result.add(new CellData(NetworkType.CDMA, cid, lac, mcc, mnc, dbm));
                } else if(cell instanceof CellInfoWcdma) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        CellInfoWcdma wcdma = (CellInfoWcdma) cell;
                        CellIdentityWcdma cellId = wcdma.getCellIdentity();
                        int cid = cellId.getCid();
                        if(cid == Integer.MAX_VALUE)
                            continue;

                        int lac = cellId.getLac();
                        int mcc = cellId.getMcc();
                        int mnc = cellId.getMnc();
                        int dbm = wcdma.getCellSignalStrength().getDbm();
                        result.add(new CellData(NetworkType.WCDMA, cid, lac, mcc, mnc, dbm));
                    }
                } else if(cell instanceof CellInfoLte) {
                    CellInfoLte lte = (CellInfoLte)cell;
                    CellIdentityLte cellId = lte.getCellIdentity();
                    int cid = cellId.getCi();
                    if(cid == Integer.MAX_VALUE)
                        continue;

                    int lac = 0;
                    int mcc = cellId.getMcc();
                    int mnc = cellId.getMnc();
                    int dbm = lte.getCellSignalStrength().getDbm();
                    result.add(new CellData(NetworkType.LTE, cid, lac, mcc, mnc, dbm));
                }
            }

            return result;
        }

        return null;
    }

    private List<WifiData> getWifiInfos() {
        if(_wifi == null || !_wifi.isWifiEnabled())
            return null;

        List<WifiData> result = new ArrayList<>();
        List<ScanResult> wifiNetworks = _wifi.getScanResults();
        if (wifiNetworks != null && wifiNetworks.size() > 0) {
            for (ScanResult net : wifiNetworks) {
                result.add(new WifiData(net.BSSID, net.level));
            }
        }

        return result;
    }

    enum NetworkType {
        NONE,
        GSM,
        CDMA,
        WCDMA,
        LTE
    }

    abstract class SignalData {
        public final int DBM;

        public SignalData(int dbm) {
            DBM = dbm;
        }
    }

    class CellData extends SignalData {
        public final NetworkType Type;
        public final int CellId;
        public final int MCC;
        public final int MNC;
        public final int LAC;

        public CellData(NetworkType type, int cellId, int lac, int mcc, int mnc, int dbm) {
            super(dbm);
            Type = type;
            CellId = cellId;
            LAC = lac;
            MCC = mcc;
            MNC = mnc;
        }
    }

    class WifiData extends SignalData {
        public final String BSSID;

        public WifiData(String ssid, int dbm) {
            super(dbm);
            BSSID = ssid;
        }
    }
}
