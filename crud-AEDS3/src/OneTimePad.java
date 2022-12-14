import java.io.IOException;

 
public class OneTimePad
{
    public static int controleSenha = 0; 
    public static String encryptionSenha(String s) throws IOException
    {
        int i, j;
        int randomBitPattern[] = new int[8];
        for (i = 0; i < 7; i++)
        {
            randomBitPattern[i] = (i % 2 == 0) ? 1 : 0;
        }
        char asc[] = new char[s.length()];
        for (i = 0; i < s.length(); i++)
        {
            asc[i] = (char) (s.charAt(i));
        }
        BasicOperation b1 = new BasicOperation();
        String cipherText = new String("");
        for (i = 0; i < asc.length; i++)
        {
            int temp = (int) (asc[i]);
            int len = b1.decimalToBinary(temp);
            int bintemp[] = new int[7];
            int xorlen;
            if (len == 7)
            {
                for (j = 1; j <= len; j++)
                {
                    bintemp[j - 1] = b1.binaryArrayAtPosition(j);
                }
                xorlen = b1.xorop(bintemp, randomBitPattern, len);
            }
            else
            {
                bintemp[0] = 0;
               for (j = 1; j <= len; j++)
                {
                    bintemp[j] = b1.binaryArrayAtPosition(j);
                }

                xorlen = b1.xorop(bintemp, randomBitPattern, len + 1);
            }
            int xor[] = new int[xorlen];
            for (j = 0; j < xorlen; j++)
            {
                xor[j] = b1.xorinArrayAt(j);
                cipherText = cipherText + xor[j];
            }
            cipherText += " ";
        }
        return (cipherText);
    }
 
    public static String decryptionSenha(String s) throws IOException
    {
        int i, j;

        char cipherChar[] = new char[(s.length())];
        int cnt = -1;
        for (i = 0; i < s.length(); i++)
        {

            if ((int) s.charAt(i) == 48 || (int) s.charAt(i) == 49
                    || (int) s.charAt(i) == 32)
            {
                cnt++;
                cipherChar[cnt] = s.charAt(i);
            }
        }
        String s1 = new String(cipherChar);
        String s2[] = s1.split(" ");
        int data[] = new int[s2.length];
        for (i = 0; i < s2.length; i++)
        {
            data[i] = Integer.parseInt(s2[i]);
        }
        char randomBitPattern[] = new char[7];
        for (i = 0; i < 7; i++)
        {
            randomBitPattern[i] = (i % 2 == 0) ? '1' : '0';
        }
        BasicOperation b1 = new BasicOperation();
        String plain = new String("");

        for (i = 0; i < s2.length; i++)
        {
            int xorlen = b1.xorop(s2[i], randomBitPattern);
            int xor[] = new int[xorlen];
            for (j = 0; j < xorlen; j++)
            {
                xor[j] = b1.xorinArrayAt(j);
                plain += xor[j];
            }
            plain += " ";
        }
        String p[] = plain.split(" ");
        BasicOperation ob = new BasicOperation();
        int decryptedChar[] = new int[p.length];
        char plainTextChar[] = new char[p.length];
        for (i = 0; i < p.length; i++)
        {
            decryptedChar[i] = ob.binaryToDecimal(Integer.parseInt(p[i]));
            plainTextChar[i] = (char) decryptedChar[i]; 
        }
        return (new String(plainTextChar));
    }
}
 
class BasicOperation
{
    int bin[]   = new int[100];
    int xor[]   = new int[100];
    int temp1[] = new int[100];
    int temp2[] = new int[100];
    int len;
    int xorlen;
 
    public int binaryToDecimal(int myNum)
    {
        int dec = 0, no, i, n = 0;
        no = myNum;
        while (no > 0)
        {
            n++;
            no = no / 10;
        }
        no = myNum;
        for (i = 0; i < n; i++)//
        {
            int temp = no % 10;
            dec = dec + temp * ((int) Math.pow(2, i));
            no = no / 10;
        }
        return dec;
    }
 
    public int decimalToBinary(int myNum)
    {
        int j, i = -1, no, temp = 0;
        no = myNum;
        int t[] = new int[100];
        while (no > 0)
        {
            i++;
            temp = no % 2;
            t[i] = temp;
            no = no / 2;
        }
        len = (i + 1);
        j = -1;
        for (i = len; i >= 0; i--)
        {
            j++;
            bin[j] = t[i];
        }
        return len;
    }

    public int binaryArrayAtPosition(int pos)
    {
        return bin[pos];
    }
 
    public int xorinArrayAt(int pos)
    {
        return xor[pos];
    }
 
    public int xorop(int a[], int b[], int arrlen)
    {
        int i;
        for (i = 0; i < arrlen; i++)
        {
            xor[i] = (a[i] == b[i]) ? 0 : 1;
        }
        xorlen = i;
        return xorlen;
    }
 
    
    public int xorop(String s, char c[])
    {
        int i = -1;
        for (i = 0; i < s.length(); i++)
        {
            xor[i] = (s.charAt(i) == c[i]) ? 0 : 1;
        }
        xorlen = i;
        return xorlen;
    }
}