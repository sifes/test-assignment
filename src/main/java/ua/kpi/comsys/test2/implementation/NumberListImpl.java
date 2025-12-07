/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 */

package ua.kpi.comsys.test2.implementation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import ua.kpi.comsys.test2.NumberList;

/**
 * Custom implementation of INumberList interface.
 * Has to be implemented by each student independently.
 *
 * Gradebook number: 13
 * C3 = 1: Circular singly-linked list
 * C5 = 3: Primary number system - Decimal (base 10)
 * C7 = 6: Additional operation - Bitwise OR
 * Alternate system: Hexadecimal (base 16)
 *
 * @author Король Олександр Володимирович, Група ІМ-32, номер в списку групи #13
 *
 */
public class NumberListImpl implements NumberList {

    /**
     * Inner Node class for circular singly-linked list.
     * Each node stores one digit as a Byte value.
     */
    private static class Node {
        Byte value;      // Single digit (0-9 for decimal, 0-15 for hex)
        Node next;       // Pointer to next node in circular list

        Node(Byte value) {
            this.value = value;
            this.next = null;
        }
    }

    // Fields
    private Node head;    // Entry point to circular list (null if empty)
    private int size;     // Number of digits (cached for O(1) access)
    private int radix;    // Current number system (10 for decimal, 16 for hex)

    /**
     * Default constructor. Returns empty <tt>NumberListImpl</tt>
     */
    public NumberListImpl() {
        this.head = null;
        this.size = 0;
        this.radix = 10;  // Always start in decimal
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * from file, defined in string format.
     *
     * @param file - file where number is stored.
     */
    public NumberListImpl(File file) {
        this();  // Initialize fields

        if (file == null || !file.exists() || !file.canRead()) {
            return;  // Empty list if file doesn't exist or can't be read
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                // Delegate to String constructor
                NumberListImpl temp = new NumberListImpl(line);
                this.head = temp.head;
                this.size = temp.size;
                this.radix = temp.radix;
            }
        } catch (IOException e) {
            // On error, leave as empty list
        }
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * in string notation.
     *
     * @param value - number in string notation.
     */
    public NumberListImpl(String value) {
        this();  // Initialize fields

        if (value == null || value.isEmpty()) {
            return;  // Empty list
        }

        // Validate input: only digits allowed, no negative numbers
        if (!value.matches("\\d+")) {
            return;  // Invalid input creates empty list
        }

        // Remove leading zeros
        int firstNonZero = 0;
        while (firstNonZero < value.length() - 1 && value.charAt(firstNonZero) == '0') {
            firstNonZero++;
        }
        value = value.substring(firstNonZero);

        // Add digits in reverse order (LSB first)
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            byte digit = (byte) (c - '0');
            add(digit);
        }
    }


    /**
     * Saves the number, stored in the list, into specified file
     * in <b>decimal</b> scale of notation.
     *
     * @param file - file where number has to be stored.
     */
    public void saveList(File file) {
        if (file == null) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(toDecimalString());
        } catch (IOException e) {
            // Silently fail on IO error
        }
    }


    /**
     * Returns student's record book number, which has 4 decimal digits.
     *
     * @return student's record book number.
     */
    public static int getRecordBookNumber() {
        return 4213;  // Gradebook #13, using 42xx range for testing
    }


    /**
     * Returns new <tt>NumberListImpl</tt> which represents the same number
     * in other scale of notation, defined by personal test assignment.<p>
     *
     * Does not impact the original list.
     *
     * For gradebook #13: C5=3 (primary=decimal), alternate=(C5+1)%5=4 (hexadecimal)
     *
     * @return <tt>NumberListImpl</tt> in other scale of notation.
     */
    public NumberListImpl changeScale() {
        // Get decimal representation
        String decimal = toDecimalString();

        // Determine the alternate radix based on getRecordBookNumber() % 5
        int primaryRadix = getRecordBookNumber() % 5;
        int alternateRadix = (primaryRadix + 1) % 5;

        // Map to actual radix values: 0=binary(2), 1=ternary(3), 2=octal(8), 3=decimal(10), 4=hex(16)
        int[] radixMap = {2, 3, 8, 10, 16};
        int targetRadix = radixMap[alternateRadix];

        // Convert decimal to target radix using BigInteger
        BigInteger num = new BigInteger(decimal);
        String converted = num.toString(targetRadix).toUpperCase();

        // Create new NumberListImpl with target radix
        NumberListImpl result = new NumberListImpl();
        result.radix = targetRadix;

        // Parse the converted string and add digits in reverse order
        for (int i = converted.length() - 1; i >= 0; i--) {
            char c = converted.charAt(i);
            byte digit;
            if (c >= '0' && c <= '9') {
                digit = (byte) (c - '0');
            } else if (c >= 'A' && c <= 'F') {
                digit = (byte) (10 + (c - 'A'));
            } else {
                continue;  // Skip invalid characters
            }

            // Directly add without validation check (we know it's valid)
            Node newNode = new Node(digit);
            if (result.head == null) {
                result.head = newNode;
                result.head.next = result.head;
            } else {
                Node tail = result.getTail();
                newNode.next = result.head;
                tail.next = newNode;
            }
            result.size++;
        }

        return result;
    }


    /**
     * Returns new <tt>NumberListImpl</tt> which represents the result of
     * additional operation, defined by personal test assignment.<p>
     *
     * Does not impact the original list.
     *
     * For gradebook #13: C7=6 → Bitwise OR operation
     *
     * @param arg - second argument of additional operation
     *
     * @return result of additional operation.
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        if (arg == null) {
            throw new NullPointerException("Argument cannot be null");
        }

        // Determine operation based on getRecordBookNumber() % 7
        int operation = getRecordBookNumber() % 7;

        // Convert both operands to decimal
        String thisDecimal = this.toDecimalString();
        String argDecimal = ((NumberListImpl) arg).toDecimalString();

        BigInteger a = new BigInteger(thisDecimal);
        BigInteger b = new BigInteger(argDecimal);
        BigInteger result;

        switch (operation) {
            case 0: // Addition
                result = a.add(b);
                break;
            case 1: // Subtraction
                result = a.subtract(b);
                if (result.compareTo(BigInteger.ZERO) < 0) {
                    result = BigInteger.ZERO;  // Clamp to zero for negative results
                }
                break;
            case 2: // Multiplication
                result = a.multiply(b);
                break;
            case 3: // Division (integer)
                if (b.equals(BigInteger.ZERO)) {
                    result = BigInteger.ZERO;
                } else {
                    result = a.divide(b);
                }
                break;
            case 4: // Modulo
                if (b.equals(BigInteger.ZERO)) {
                    result = BigInteger.ZERO;
                } else {
                    result = a.mod(b);
                }
                break;
            case 5: // Bitwise AND
                result = a.and(b);
                break;
            case 6: // Bitwise OR
                result = a.or(b);
                break;
            default:
                result = BigInteger.ZERO;
        }

        // Create new NumberListImpl from result
        return new NumberListImpl(result.toString());
    }


    /**
     * Returns string representation of number, stored in the list
     * in <b>decimal</b> scale of notation.
     *
     * @return string representation in <b>decimal</b> scale.
     */
    public String toDecimalString() {
        if (isEmpty()) {
            return "0";
        }

        if (radix == 10) {
            // Already in decimal, just return toString()
            return toString();
        } else if (radix == 16) {
            // Convert from hexadecimal to decimal
            // Build the hex string from our list
            StringBuilder hexStr = new StringBuilder();
            Node current = head;
            for (int i = 0; i < size; i++) {
                byte digit = current.value;
                if (digit >= 10) {
                    hexStr.append((char) ('A' + (digit - 10)));
                } else {
                    hexStr.append(digit);
                }
                current = current.next;
            }

            // Reverse to get proper order
            String hex = hexStr.reverse().toString();

            // Remove leading zeros
            int firstNonZero = 0;
            while (firstNonZero < hex.length() - 1 && hex.charAt(firstNonZero) == '0') {
                firstNonZero++;
            }
            hex = hex.substring(firstNonZero);

            // Convert hex to decimal using BigInteger
            BigInteger num = new BigInteger(hex, 16);
            return num.toString(10);
        }

        return "0";
    }


    @Override
    public String toString() {
        if (isEmpty()) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        Node current = head;
        for (int i = 0; i < size; i++) {
            byte digit = current.value;
            if (radix == 16 && digit >= 10) {
                // Convert 10-15 to A-F
                sb.append((char) ('A' + (digit - 10)));
            } else {
                sb.append(digit);
            }
            current = current.next;
        }

        // Reverse the string (we store digits in reverse order)
        String result = sb.reverse().toString();

        // Remove leading zeros
        int firstNonZero = 0;
        while (firstNonZero < result.length() - 1 && result.charAt(firstNonZero) == '0') {
            firstNonZero++;
        }

        return result.substring(firstNonZero);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof NumberList)) {
            return false;
        }

        NumberListImpl other = (NumberListImpl) o;

        // Compare sizes first
        if (this.size != other.size) {
            return false;
        }

        // Compare decimal representations (works across different radixes)
        return this.toDecimalString().equals(other.toDecimalString());
    }


    // ========== Helper Methods ==========

    /**
     * Helper method to get node at specified index.
     * @param index The index of the node to retrieve
     * @return The node at the specified index
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    private Node getNode(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    /**
     * Helper method to find the tail node (last node in circular list).
     * @return The tail node, or null if list is empty
     */
    private Node getTail() {
        if (head == null) return null;
        Node tail = head;
        while (tail.next != head) {
            tail = tail.next;
        }
        return tail;
    }

    /**
     * Helper method to validate if a digit is valid for the current radix.
     * @param digit The digit to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidDigit(Byte digit) {
        if (digit == null) return false;
        return digit >= 0 && digit < radix;
    }

    // ========== List Interface Methods ==========

    @Override
    public int size() {
        return size;
    }


    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }


    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            private Node current = head;
            private int count = 0;

            @Override
            public boolean hasNext() {
                return count < size;
            }

            @Override
            public Byte next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Byte value = current.value;
                current = current.next;
                count++;
                return value;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove not supported");
            }
        };
    }


    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        Node current = head;
        for (int i = 0; i < size; i++) {
            array[i] = current.value;
            current = current.next;
        }
        return array;
    }


    @Override
    public <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public boolean add(Byte e) {
        if (e == null) {
            throw new NullPointerException("Null elements are not permitted");
        }
        if (!isValidDigit(e)) {
            throw new IllegalArgumentException("Digit " + e + " is invalid for radix " + radix);
        }

        Node newNode = new Node(e);

        if (head == null) {
            // First node - create circular reference
            head = newNode;
            head.next = head;
        } else {
            // Find tail and insert after it
            Node tail = getTail();
            newNode.next = head;
            tail.next = newNode;
        }

        size++;
        return true;
    }


    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Byte)) {
            return false;
        }

        int index = indexOf(o);
        if (index < 0) {
            return false;
        }

        remove(index);
        return true;
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        boolean modified = false;
        for (Byte b : c) {
            if (add(b)) {
                modified = true;
            }
        }
        return modified;
    }


    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        boolean modified = false;
        int currentIndex = index;
        for (Byte b : c) {
            add(currentIndex++, b);
            modified = true;
        }
        return modified;
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        boolean modified = false;
        for (Object o : c) {
            while (remove(o)) {
                modified = true;
            }
        }
        return modified;
    }


    @Override
    public boolean retainAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        boolean modified = false;
        Node current = head;
        int i = 0;
        while (i < size) {
            if (!c.contains(current.value)) {
                remove(i);
                modified = true;
                current = (i < size && head != null) ? getNode(i) : null;
            } else {
                current = current.next;
                i++;
            }
        }
        return modified;
    }


    @Override
    public void clear() {
        head = null;
        size = 0;
        // radix stays the same
    }


    @Override
    public Byte get(int index) {
        return getNode(index).value;
    }


    @Override
    public Byte set(int index, Byte element) {
        if (element == null) {
            throw new NullPointerException("Null elements are not permitted");
        }
        if (!isValidDigit(element)) {
            throw new IllegalArgumentException("Digit " + element + " is invalid for radix " + radix);
        }

        Node node = getNode(index);
        Byte oldValue = node.value;
        node.value = element;
        return oldValue;
    }


    @Override
    public void add(int index, Byte element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (element == null) {
            throw new NullPointerException("Null elements are not permitted");
        }
        if (!isValidDigit(element)) {
            throw new IllegalArgumentException("Digit " + element + " is invalid for radix " + radix);
        }

        if (index == size) {
            // Add at end
            add(element);
            return;
        }

        Node newNode = new Node(element);

        if (index == 0) {
            // Insert at head
            if (head == null) {
                head = newNode;
                head.next = head;
            } else {
                Node tail = getTail();
                newNode.next = head;
                tail.next = newNode;
                head = newNode;
            }
        } else {
            // Insert in middle
            Node prev = getNode(index - 1);
            newNode.next = prev.next;
            prev.next = newNode;
        }

        size++;
    }


    @Override
    public Byte remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Byte removedValue;

        if (size == 1) {
            // Removing the only element
            removedValue = head.value;
            head = null;
        } else if (index == 0) {
            // Removing head
            Node tail = getTail();
            removedValue = head.value;
            head = head.next;
            tail.next = head;
        } else {
            // Removing from middle or end
            Node prev = getNode(index - 1);
            Node toRemove = prev.next;
            removedValue = toRemove.value;
            prev.next = toRemove.next;
        }

        size--;
        return removedValue;
    }


    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Byte) || head == null) {
            return -1;
        }

        Node current = head;
        for (int i = 0; i < size; i++) {
            if (current.value.equals(o)) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }


    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof Byte) || head == null) {
            return -1;
        }

        int lastIndex = -1;
        Node current = head;
        for (int i = 0; i < size; i++) {
            if (current.value.equals(o)) {
                lastIndex = i;
            }
            current = current.next;
        }
        return lastIndex;
    }


    @Override
    public ListIterator<Byte> listIterator() {
        return listIterator(0);
    }


    @Override
    public ListIterator<Byte> listIterator(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        return new ListIterator<Byte>() {
            private int currentIndex = index;
            private int lastReturnedIndex = -1;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public Byte next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                lastReturnedIndex = currentIndex;
                return get(currentIndex++);
            }

            @Override
            public boolean hasPrevious() {
                return currentIndex > 0;
            }

            @Override
            public Byte previous() {
                if (!hasPrevious()) {
                    throw new NoSuchElementException();
                }
                lastReturnedIndex = --currentIndex;
                return get(currentIndex);
            }

            @Override
            public int nextIndex() {
                return currentIndex;
            }

            @Override
            public int previousIndex() {
                return currentIndex - 1;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void set(Byte e) {
                if (lastReturnedIndex < 0) {
                    throw new IllegalStateException();
                }
                NumberListImpl.this.set(lastReturnedIndex, e);
            }

            @Override
            public void add(Byte e) {
                NumberListImpl.this.add(currentIndex++, e);
                lastReturnedIndex = -1;
            }
        };
    }


    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + ", toIndex: " + toIndex + ", size: " + size);
        }

        NumberListImpl sublist = new NumberListImpl();
        sublist.radix = this.radix;

        for (int i = fromIndex; i < toIndex; i++) {
            sublist.add(get(i));
        }

        return sublist;
    }


    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) {
            return false;
        }

        if (index1 == index2) {
            return true;  // Nothing to swap
        }

        // Get both nodes and swap their values
        Node node1 = getNode(index1);
        Node node2 = getNode(index2);

        Byte temp = node1.value;
        node1.value = node2.value;
        node2.value = temp;

        return true;
    }


    @Override
    public void sortAscending() {
        if (size <= 1) {
            return;
        }

        // Convert to array, sort, and rebuild
        Byte[] arr = new Byte[size];
        Node current = head;
        for (int i = 0; i < size; i++) {
            arr[i] = current.value;
            current = current.next;
        }

        // Simple bubble sort (ascending)
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    Byte temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }

        // Rebuild list from sorted array
        current = head;
        for (int i = 0; i < size; i++) {
            current.value = arr[i];
            current = current.next;
        }
    }


    @Override
    public void sortDescending() {
        if (size <= 1) {
            return;
        }

        // Convert to array, sort, and rebuild
        Byte[] arr = new Byte[size];
        Node current = head;
        for (int i = 0; i < size; i++) {
            arr[i] = current.value;
            current = current.next;
        }

        // Simple bubble sort (descending)
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] < arr[j + 1]) {
                    Byte temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }

        // Rebuild list from sorted array
        current = head;
        for (int i = 0; i < size; i++) {
            current.value = arr[i];
            current = current.next;
        }
    }


    @Override
    public void shiftLeft() {
        if (size <= 1) {
            return;  // Nothing to shift
        }

        // In a circular list, shift left means move head to next
        head = head.next;
    }


    @Override
    public void shiftRight() {
        if (size <= 1) {
            return;  // Nothing to shift
        }

        // In a circular list, shift right means move head to previous (tail)
        head = getTail();
    }
}
