package com.github.oxaoo.lexer;

import java.util.*;

/**
 * Created by Alexander on 01.11.2015.
 */
public class CLexer
{
    private enum status{GOOD, ERROR, END};

    private char[] m_text = null;
    private int m_index = 0;

    private Set<Character> m_CNum = new HashSet<Character>(10);
    private Set<Character> m_CChar = new HashSet<Character>(53);
    private Set<Character> m_CSep = new HashSet<Character>(9);
    private Set<Character> m_COper = new HashSet<Character>(6);
    private Set<Character> m_CLog = new HashSet<Character>(4);
    private Set<Character> m_CAddit = new HashSet<Character>(2);

    private List<String> m_letters = new ArrayList<String>(100);
    private StringBuilder m_buffer = new StringBuilder();

    public CLexer(char[] text)
    {
        //���������� ������ ������ "������".
        for (int c = 65; c < 123; c++)
        {
            if (c > 90 && c < 97 && c != 95)
                continue;

            m_CChar.add((char) c);
        }

        //���������� ������ ������ "�����".
        for (int n = 48; n < 57; n++)
        {
            m_CNum.add((char) n);
        }

        //���������� ������ ������ "�����������".
        Collections.addAll(m_CSep, '.', ',', ';', '(', ')', '[', ']', '{', '}');

        //���������� ������ ������ "��������".
        Collections.addAll(m_COper, '!', '=', '<', '>', '/', '*');

        //���������� ������ ������ "����������".
        Collections.addAll(m_CLog, '&', '|', ':', '?');

        //���������� ������ ������ "����������".
        Collections.addAll(m_CAddit, '+', '-');

        //�������� �������� ��������.
        removeExcess(text);
    }

    //1 ���� - ������������ ������.
    public void toScan()
    {
        if (m_text.length == 0)
            return;

        status st = status.END;

        do
        {
            st = stateStart();
            if (st == status.GOOD)
            {
                m_letters.add(m_buffer.toString());
                m_buffer.setLength(0);
            }

            if (st == status.ERROR)
            {
                System.out.println("Can not recognize the lexeme.");
                break;
            }
        }
        while(st != status.END);

    }

    private status stateStart()
    {
        if (m_CNum.contains(m_text[m_index]))
            return stateNorf();

        if (m_text[m_index] == '-')
            return stateEqordec();

        if (m_text[m_index] == '+')
            return stateEqorinc();

        if (m_COper.contains(m_text[m_index]))
            return stateEq();

        if (m_CSep.contains(m_text[m_index]))
            return stateSep();

        if (m_CChar.contains(m_text[m_index]))
            return stateId();

        if (m_CLog.contains(m_text[m_index]))
            return stateLog();

        return status.ERROR;
    }

    private status stateId()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        if (m_CNum.contains(m_text[m_index])
                || m_CChar.contains(m_text[m_index]))
            return stateId();

        if (m_text[m_index] == '.')
            return stateDot();

        return status.GOOD;
    }

    private status stateDot()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        if (m_CChar.contains(m_text[m_index]))
            return stateVar();

        return status.ERROR;
    }

    private status stateVar()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        if (m_CNum.contains(m_text[m_index])
                || m_CChar.contains(m_text[m_index]))
            return stateVar();

        return status.GOOD;
    }

    private status stateLog()
    {
        if (m_index >= m_text.length)
            return status.END;

        char temp = m_text[m_index];
        m_buffer.append(m_text[m_index++]);

        if (temp == '?' || temp == ':')
            return status.GOOD;

        /*
        if (m_text[m_index] == '&' && temp == '&')
            return stateAnd2();

        if (m_text[m_index] == '|' && temp == '|')
            return stateOr2();
        */

        if (m_text[m_index] == '&' && temp == '&'
                || m_text[m_index] == '|' && temp == '|')
            return state2();

        return status.ERROR;
    }

    private status state2()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        return status.GOOD;
    }

    /*
    private status stateOr2()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        return status.GOOD;
    }

    private status stateAnd2()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        return status.GOOD;
    }
    */

    private status stateSep()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        return status.GOOD;
    }

    private status stateEq()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        if (m_text[m_index] == '=')
            return stateEnd();

        return status.GOOD;
    }

    private status stateEqorinc()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        if (m_text[m_index] == '=' || m_text[m_index] == '+')
            return stateEnd();

        return status.GOOD;
    }

    private status stateEqordec()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        if (m_CNum.contains(m_text[m_index]))
            return stateNorf();

        if (m_text[m_index] == '=' || m_text[m_index] == '-')
            return stateEnd();

        return status.GOOD;
    }

    private status stateEnd()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        return status.GOOD;
    }

    private status stateNorf()
    {
        m_buffer.append(m_text[m_index++]);

        if (m_CNum.contains(m_text[m_index]))
            return stateNum();

        if (m_text[m_index] == '.')
            return stateFrac();

        return status.GOOD;
    }

    private status stateNum()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        if (m_CNum.contains(m_text[m_index]))
            return stateNum();

        if (m_text[m_index] == '.')
            return stateFrac();

        return status.GOOD;
    }

    private status stateFrac()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        if (m_CNum.contains(m_text[m_index]))
            return stateFnum();

        return status.ERROR;
    }

    private status stateFnum()
    {
        if (m_index >= m_text.length)
            return status.END;

        m_buffer.append(m_text[m_index++]);

        if (m_CNum.contains(m_text[m_index]))
            return stateFnum();

        return status.GOOD;
    }

    public void toEstimate()
    {

    }

    public void result()
    {

    }

    /*
    �������� ������������
    � ������ ���������� ��������.
     */
    private void removeExcess(char[] text)
    {
        //TODO: ��������� ��������.
        m_text = text;
    }
}
