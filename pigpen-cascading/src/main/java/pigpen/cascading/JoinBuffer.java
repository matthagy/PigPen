package pigpen.cascading;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import clojure.lang.IFn;
import clojure.lang.LazySeq;
import clojure.lang.PersistentVector;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Buffer;
import cascading.operation.BufferCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class JoinBuffer extends BaseOperation implements Buffer {

  private final String init;
  private final String func;

  public JoinBuffer(String init, String func, Fields fields) {
    super(fields);
    this.init = init;
    this.func = func;
  }

  @Override
  public void prepare(FlowProcess flowProcess, OperationCall operationCall) {
    super.prepare(flowProcess, operationCall);
    OperationUtil.init(init);
    IFn fn = OperationUtil.getFn(func);
    operationCall.setContext(fn);
  }

  @Override
  public void operate(FlowProcess flowProcess, BufferCall bufferCall) {
    IFn fn = (IFn)bufferCall.getContext();
    Iterator<TupleEntry> iterator = bufferCall.getArgumentsIterator();
    while (iterator.hasNext()) {
      TupleEntry entry = iterator.next();
      List args = new ArrayList(2);
      args.add(entry.getObject(1));
      args.add(entry.getObject(3));
      // TODO: do this in clojure
      LazySeq result = (LazySeq)fn.invoke(args);
      for (Object obj : result) {
        bufferCall.getOutputCollector().add(new Tuple(((PersistentVector)obj).toArray()));
      }
    }
  }
}
