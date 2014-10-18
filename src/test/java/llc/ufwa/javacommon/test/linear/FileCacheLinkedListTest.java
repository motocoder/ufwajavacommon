package llc.ufwa.javacommon.test.linear;

import java.io.File;
import java.util.LinkedList;

import junit.framework.TestCase;
import llc.ufwa.data.exception.FileCacheLinkedListException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.linear.FileCacheLinkedList;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

public class FileCacheLinkedListTest {

	static {
		BasicConfigurator.configure();
	}

	@Test
	public void basicTest() throws ResourceException, FileCacheLinkedListException {

		final File folder = new File("./target/test-files/linkedlist/");

		FileCacheLinkedList<String> list = new FileCacheLinkedList<String>(folder);
		list.clear();

		list.add("B");
		list.add("C");
		list.add("D");
		list.add("A");

		TestCase.assertEquals(4, list.size());

		TestCase.assertEquals("B", list.peek());
		TestCase.assertEquals("B", list.peek());
		TestCase.assertEquals("B", list.pop());
		TestCase.assertEquals("C", list.pop());
		TestCase.assertEquals("D", list.peek());
		TestCase.assertEquals("D", list.pop());
		TestCase.assertEquals("A", list.pop());

		TestCase.assertEquals(0, list.size());

		list.add("ABC");
		list.add("G");
		list.add("F");
		list.add("T");
		list.add("PP");
		list.add("ABC");

		TestCase.assertEquals(0, list.indexOf("ABC"));
		TestCase.assertEquals(2, list.indexOf("F"));
		TestCase.assertEquals(3, list.indexOf("T"));
		TestCase.assertEquals("ABC", list.remove());
		TestCase.assertEquals(2 - 1, list.indexOf("F"));
		TestCase.assertEquals(3 - 1, list.indexOf("T"));

		TestCase.assertEquals(true, list.contains("PP"));

		TestCase.assertEquals(5, list.size());

		list.clear();

		TestCase.assertEquals(0, list.size());

		list.add("ABC");
		list.add("G");
		list.add("F");
		list.add("ABC");

		TestCase.assertEquals(4, list.size());

		final Object[] listArray = list.toArray();
		TestCase.assertEquals("ABC", listArray[0]);
		TestCase.assertEquals("G", listArray[1]);
		TestCase.assertEquals("F", listArray[2]);
		TestCase.assertEquals("ABC", listArray[3]);

		TestCase.assertEquals("ABC", list.getFirst());
		TestCase.assertEquals("ABC", list.getLast());
		TestCase.assertEquals(3, list.lastIndexOf("ABC"));
		TestCase.assertEquals(1, list.lastIndexOf("G"));

		list.clear();

		list.add("ABC");
		list.add("G");
		list.add("F");
		list.add("T");
		list.add("PP");

		TestCase.assertEquals(5, list.size());
		TestCase.assertEquals("PP", list.get(4));

		list.add(3, "TEST");
		TestCase.assertEquals(6, list.size());
		TestCase.assertEquals(3, list.indexOf("TEST"));

		list.clear();

		LinkedList<String> array = new LinkedList<String>();
		array.add("1");
		array.add("2");
		array.add("3");
		list.addAll(array);

		TestCase.assertEquals(3, list.size());

		list.add("F");

		TestCase.assertEquals(4, list.size());
		TestCase.assertEquals(true, list.contains("F"));
		TestCase.assertEquals(true, list.remove("F"));
		TestCase.assertEquals(false, list.contains("F"));
		TestCase.assertEquals(3, list.size());
		TestCase.assertEquals("3", list.remove(2));
		TestCase.assertEquals("2", list.remove(1));
		TestCase.assertEquals("1", list.remove(0));
		TestCase.assertEquals(0, list.size());

		list.add("FG");
		list.add("FR");
		list.add("TSLJFLJDLJFLJDIOOFIJEKOPJAUSAYUDOIA");
		TestCase.assertEquals(3, list.size());
		list.add(2, "LKJLJD");
		TestCase.assertEquals(2, list.indexOf("LKJLJD"));
		TestCase.assertEquals(4, list.size());

		list.addFirst("ELJR");
		TestCase.assertEquals(0, list.indexOf("ELJR"));

		list.addLast("LJ");
		TestCase.assertEquals(5, list.indexOf("LJ"));

		list.clear();
		TestCase.assertEquals(true, list.isEmpty());

		list.add("B");
		list.add("C");
		list.add("D");
		list.add("A");

		TestCase.assertEquals("A", list.pollLast());
		TestCase.assertEquals("B", list.getFirst());
		TestCase.assertEquals("D", list.pollLast());

		list.offerFirst("OIIF");
		TestCase.assertEquals(0, list.indexOf("OIIF"));

		list.push("UIO");
		TestCase.assertEquals(0, list.indexOf("UIO"));

		final String at3 = list.get(2);
		TestCase.assertEquals(at3, list.subList(2, 4).get(0));

		list.removeAll(list.subList(2, 4));
		TestCase.assertEquals(2, list.size());

		list.clear();

		list.add("B");
		list.add("C");
		list.add("D");
		list.add("A");

		TestCase.assertEquals("D", list.get(2));
		list.set(2, "H");
		TestCase.assertEquals("H", list.get(2));

		list.retainAll(list.subList(1, 2));
		TestCase.assertEquals(1, list.size());

		list.clear();

		list.add("C");
		list.add("D");
		list.add("C");
		list.add("D");
		list.add("C");

		TestCase.assertEquals("C", list.get(2));

		TestCase.assertEquals("C;D;C;D;C;", list.toString());

		TestCase.assertEquals(5, list.size());

	}

	@Test
	public void persistenceTest() throws ResourceException, FileCacheLinkedListException, InterruptedException {

		final File folder = new File("./target/test-files/linkedlist/");

		FileCacheLinkedList<String> list = new FileCacheLinkedList<String>(folder);
		list.clear();

		list.add("B");
		list.add("C");
		list.add("D");
		list.add("A");

		list = null;

		Thread.sleep(3000);

		FileCacheLinkedList<String> list2 = new FileCacheLinkedList<String>(folder);

		TestCase.assertEquals(4, list2.size());

		TestCase.assertEquals("B", list2.peek());
		TestCase.assertEquals("B", list2.peek());
		TestCase.assertEquals("B", list2.pop());
		TestCase.assertEquals("C", list2.pop());
		TestCase.assertEquals("D", list2.peek());
		TestCase.assertEquals("D", list2.pop());
		TestCase.assertEquals("A", list2.pop());

		TestCase.assertEquals(0, list2.size());

	}

}
