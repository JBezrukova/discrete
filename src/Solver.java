import javax.sound.midi.Soundbank;
import javax.swing.*;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by bezru on 28.05.2017.
 */
public class Solver {

    private static BigInteger a;
    private static BigInteger t;
    private static BigInteger q;
    private static BigInteger q1;
    private static HashMap<BigInteger, BigInteger> pMap;
    private static TreeMap<BigInteger, List<BigInteger>> numberToResMap;


    public Solver(BigInteger a, BigInteger t, BigInteger q) {
        this.a = a;
        this.t = t;
        this.q = q;
        System.out.println("Входные данные: " + "a = " + a + "; t = " + t + "; q = " + q + ";");
        findLog();
    }

    private static void checkIfPrime() {
        if (q.isProbablePrime(1) == false) {
            JOptionPane.showMessageDialog(null,
                    new String[]{"Ошибка работы алгоритма :",
                            "Число не прошло проверку на простоту",
                    },
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static List<BigInteger> getList() {
        List<BigInteger> list = pMap.keySet().stream().collect(Collectors.toList());
        list.sort(BigInteger::compareTo);
        return list;
    }

    private static HashMap<BigInteger, BigInteger> trialDivisionMethod(BigInteger n) {

        HashMap<BigInteger, BigInteger> divs = new HashMap();
        if (n.compareTo(BigInteger.ONE) < 0) {
            System.out.println("Can not be solved by trial division method");
        } else {

            while (n.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
                n = n.divide(BigInteger.valueOf(2));
                if (divs.get(BigInteger.valueOf(2)) != null) {
                    divs.computeIfPresent(BigInteger.valueOf(2), (k, v) -> v.add(BigInteger.ONE));
                } else {
                    divs.put(BigInteger.valueOf(2), BigInteger.ONE);
                }
            }
            BigInteger d = BigInteger.valueOf(3);
            while (d.pow(2).compareTo(n) <= 0) {
                while (n.mod(d).compareTo(BigInteger.ZERO) == 0) {
                    n = n.divide(d);
                    if (divs.get(d) != null) {
                        divs.computeIfPresent(d, (k, v) -> v.add(BigInteger.ONE));
                    } else {
                        divs.put(d, BigInteger.ONE);
                    }
                }
                d = d.add(BigInteger.valueOf(2));
            }
            if (n.compareTo(BigInteger.ONE) == 0) {
                return divs;
            } else {
                if (divs.get(n) != null) {
                    divs.computeIfPresent(n, (k, v) -> v.add(BigInteger.ONE));
                } else {
                    divs.put(n, BigInteger.ONE);
                }
                return divs;
            }
        }
        return divs;
    }

    private static void tableCreate() {
        List<BigInteger> list = getList();
        int k = 0;
        numberToResMap = new TreeMap<>();


        for (int i = 0; i < list.size(); i++) {
            k = list.get(i).subtract(BigInteger.ONE).intValue();
            List<BigInteger> list1 = new ArrayList<>();
            int j = 0;
            while (j <= k) {
                list1.add(t.modPow(((BigInteger.valueOf(j).multiply(q1)).divide(list.get(i))), q));
                j++;
            }
            numberToResMap.put(list.get(i), list1);
        }
    }

    public static void mapsout(TreeMap<BigInteger, List<BigInteger>> map) {
        Iterator iterator = map.entrySet().iterator();
        System.out.println("Таблица вычетов:");
        while (iterator.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) iterator.next();
            System.out.println("Множитель = " + pair.getKey() + "  =>  Вычеты = " + pair.getValue());
        }
    }

    public static void findLog() {
        double startTime = System.nanoTime();
        checkIfPrime();
        double timeSpent = (System.nanoTime() - startTime) / Math.pow(10, 6);
        System.out.println("Проверка на простоту: " + timeSpent);
        q1 = q.subtract(BigInteger.ONE);
        MainFrame.getInstance().setQ1Value(q1);

        startTime = System.nanoTime();
        pMap = trialDivisionMethod(q1);
        timeSpent = (System.nanoTime() - startTime) / Math.pow(10, 6);
        System.out.println("Разложение на множители: " + timeSpent);

        MainFrame.getInstance().setFactorTable(pMap);
        toString(pMap);

        startTime = System.nanoTime();
        tableCreate();
        timeSpent = (System.nanoTime() - startTime) / Math.pow(10, 6);

        mapsout(numberToResMap);
        System.out.println("Составление таблицы вычетов: " + timeSpent);
        MainFrame.getInstance().setGraphic(numberToResMap.keySet());
        MainFrame.getInstance().setResiduesTable(numberToResMap);

        BigInteger[] mArray = createMArray();

        startTime = System.nanoTime();
        BigInteger[] nArray = createNArray();
        timeSpent = (System.nanoTime() - startTime) / Math.pow(10, 6);
        System.out.println("Поиск индексов таблицы: " + timeSpent);
        MainFrame.getInstance().mTable(mArray);
        MainFrame.getInstance().nTable(nArray);
        System.out.println("Результат решения с помощью алгоритма Гарнера (Китайская теорема об остатках):");
        startTime = System.nanoTime();
        CRTAAlgorithm(mArray, nArray);
        timeSpent = (System.nanoTime() - startTime) / Math.pow(10, 6);
        System.out.println("Китайская теорема об остатках: " + timeSpent);
    }

    private static BigInteger[] createNArray() {
        Iterator it = pMap.entrySet().iterator();
        BigInteger powValue;
        BigInteger keyValue;
        BigInteger checkValue;
        BigInteger[] nArray = new BigInteger[pMap.keySet().size()];

        int j = 0;
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            powValue = (BigInteger) pair.getValue();
            keyValue = (BigInteger) pair.getKey();
            BigInteger temp = BigInteger.ZERO;
            int index;
            for (int i = 1; i <= powValue.intValue(); i++) {
                checkValue = a.divide(t.pow(temp.intValue())).modPow(q1.divide(keyValue.pow(i)), q);
                index = numberToResMap.get(keyValue).indexOf(checkValue);
                if (index >= 0) {
                    temp = temp.add(BigInteger.valueOf(index).multiply(keyValue.pow(i - 1)));
                } else {
                    JOptionPane.showMessageDialog(MainFrame.getInstance().getContentPane(),
                            new String[]{"Ошибка работы алгоритма :",
                                    "Не найдено соответствие в таблице",
                                    "Значение: " + checkValue.toString(),
                                    "В строке p = " + keyValue},
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    throw new NullPointerException("Index is not exists");
                }
            }
            nArray[j++] = temp;
        }
        return nArray;
    }


    private static BigInteger[] createMArray() {
        BigInteger[] mArray = new BigInteger[pMap.keySet().size()];
        Iterator it = pMap.entrySet().iterator();
        int i = 0;
        BigInteger key;
        BigInteger value;
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            key = (BigInteger) pair.getKey();
            value = (BigInteger) pair.getValue();
            mArray[i++] = key.pow(value.intValue());
        }
        System.out.println("Значения модулей для вычислений в Китайской теореме об остатках:");
        System.out.println(Arrays.toString(mArray));
        return mArray;
    }


    public static BigInteger[][] inverses(BigInteger[] array) {
        BigInteger[][] inv = new BigInteger[array.length][array.length];
        for (int i = 0; i < array.length; ++i) {
            for (int j = i + 1; j < array.length; ++j) {
                inv[i][j] = array[i].modInverse(array[j]);
            }
        }
        return inv;
    }

    public static BigInteger CRTAAlgorithm(BigInteger[] a, BigInteger[] r) {
        BigInteger[][] inverses = inverses(a);
        BigInteger[] x = new BigInteger[a.length];
        for (int i = 0; i < a.length; i++) {
            x[i] = r[i];
            for (int j = 0; j < i; j++) {
                x[i] = inverses[j][i].multiply(x[i].subtract(x[j]));
                x[i] = x[i].mod(a[i]);
                if (x[i].compareTo(BigInteger.ZERO) < 0)
                    x[i] = x[i].add(a[i]);
            }
        }
        BigInteger result = x[0];
        BigInteger u;
        for (int i = 1; i < x.length; i++) {
            u = BigInteger.ONE;
            for (int j = 0; j < i; j++) {
                u = u.multiply(a[j]);
            }
            result = result.add(x[i].multiply(u));
        }
        MainFrame.getInstance().writeDLValue(result);
        return result;
    }

    public static void toString(HashMap<BigInteger, BigInteger> map) {
        Iterator it = map.entrySet().iterator();
        System.out.println("Резульат разложения на множители:");
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            System.out.println("Множитель = " + pair.getKey() + "  =>  Степень = " + pair.getValue());
        }
    }
}
