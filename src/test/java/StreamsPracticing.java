import jdk.jfr.Event;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamsPracticing {
    private List<Employee> employees = List.of(
            new Employee("Artem", "Kolchan", 1, 20, Position.CHEF),
            new Employee("Viktor", "Dudka", 2, 25, Position.MANAGER),
            new Employee("Randall", "Miserable", 3, 34, Position.MANAGER),
            new Employee("Yogi", "Bear", 4, 40, Position.WORKER),
            new Employee("Sofia", "Kulka", 5, 47, Position.WORKER),
            new Employee("Irina", "Ilnitska", 6, 60, Position.WORKER)
    );
    private List<Department> departments = List.of(
            new Department(1, 0, "Head"),
            new Department(2, 1, "West"),
            new Department(3, 1, "East"),
            new Department(4, 2, "Poland"),
            new Department(5, 2, "Ukraine")
    );

    @Test
    public void creation() throws IOException {
        Stream<String> lines = Files.lines(Paths.get("some.txt"));
        Stream<Path> lists = Files.list(Paths.get("./"));
        Stream<Path> walk = Files.walk(Paths.get("./"), 3);

        IntStream intStream = IntStream.of(1, 2, 3, 4);
        DoubleStream doubleStream = DoubleStream.of(22.4, 23, 4);
        IntStream range = IntStream.range(10, 100);//10..99
        IntStream intStream1 = IntStream.rangeClosed(10, 100);//10..100

        int[] ints = {1, 2, 3, 4};
        IntStream stream = Arrays.stream(ints);

        Stream<String> stringStream = Stream.of("1", "2", "3");
        Stream<? extends Serializable> stream1 = Stream.of(1, "2", "3");

        Stream<String> build = Stream.<String>builder()
                .add("mike")
                .add("joe")
                .build();

        Stream<Employee> stream2 = employees.stream();
        Stream<Employee> employeeStream = employees.parallelStream();
//        Stream.generate(() -> new Event(UUID.randomUUID(), LocalDateTime.now(), ""));
        Stream<Integer> iterate = Stream.iterate(1950, val -> val + 3);//1950,1953..
        Stream.concat(stringStream, build);
    }

    @Test
    public void terminate() {
        employees.stream().count();

        employees.forEach(employee -> System.out.println(employee.getAge()));
        employees.stream().forEach(employee -> System.out.println(employee.getAge()));
        employees.stream().forEachOrdered(employee -> System.out.println(employee.getAge()));
        employees.stream().collect(Collectors.toList());
        employees.stream().toArray();
        Map<Long, String> collectMap = employees.stream().collect(Collectors.toMap(
                Employee::getId,
                emp -> String.format("%s %s", emp.getFirstName(), emp.getLastName())
        ));
        employees.stream().max(Comparator.comparingInt(Employee::getAge));
        employees.stream().findAny();
        employees.stream().findFirst();
        employees.stream().noneMatch(employee -> employee.getAge() > 60);//true
        employees.stream().allMatch(employee -> employee.getAge() > 18);//true
        employees.stream().anyMatch(employee -> employee.getPosition() == Position.CHEF);//true

        IntStream intStream = IntStream.of(100, 200, 300);
        intStream.reduce(((left, right) -> left + right)).orElse(0);
        IntStream.of(100, 200, 300).average();
        IntStream.of(100, 200, 300).max();
        IntStream.of(100, 200, 300).min();
        IntStream.of(100, 200, 300).sum();
        IntStream.of(100, 200, 300).summaryStatistics();

        departments.stream().reduce(this::reducer);
    }

    @Test
    public void transform() {
        IntStream.of(1, 2, 3, 4).mapToLong(Long::valueOf);
//        IntStream.of(1,2,3,4).mapToObj(value -> );
        IntStream.of(1, 1, 2, 34, 5).distinct();//duplicates taken away
        IntStream.of(100, 200, 300, 400).flatMap(value -> IntStream.of(value - 50, value)).forEach(System.out::println);

        Stream<Employee> employeeStream = employees.stream().filter(employee -> employee.getPosition() != Position.CHEF);
        employees.stream().skip(3);
        employees.stream().limit(5);
        employees.stream()
                .sorted(Comparator.comparingInt(Employee::getAge))
                .peek(employee -> employee.setAge(18))
                .map(employee -> String.format("%s %s", employee.getFirstName(), employee.getLastName()));
        employees.stream().takeWhile(employee -> employee.getAge() > 30).forEach(System.out::println);
        employees.stream().dropWhile(employee -> employee.getAge() > 30).forEach(System.out::println);


    }

    @Test
    public void real() {
        Stream<Employee> sorted = employees.stream().filter(employee -> employee.getAge() <= 30 && employee.getPosition() != Position.WORKER)
                .sorted(Comparator.comparing(Employee::getLastName));
        print(sorted);

        Stream<Employee> sorted1 = employees.stream().filter(employee -> employee.getAge() > 40)
                .sorted(Comparator.comparing(Employee::getAge))
                .limit(4);
        print(sorted1);

        IntSummaryStatistics intSummaryStatistics = employees.stream().mapToInt(Employee::getAge).summaryStatistics();
        System.out.println(intSummaryStatistics);
    }

    private void print(Stream<Employee> stream) {
        stream.map(emp -> String.format("%s %s %s %s %s", emp.getId(), emp.getFirstName(), emp.getLastName(), emp.getAge(), emp.getPosition()))
                .forEach(System.out::println);
        System.out.println();
    }

    public Department reducer(Department parent, Department child) {
        if (child.getParent() == parent.getId()) {
            parent.getChild().add(child);
        } else {
            parent.getChild().forEach(subParent -> reducer(subParent, child));
        }
        return parent;
    }
}
