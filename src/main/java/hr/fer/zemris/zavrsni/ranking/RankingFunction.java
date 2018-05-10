package hr.fer.zemris.zavrsni.ranking;

import hr.fer.zemris.zavrsni.model.Result;

import java.io.IOException;
import java.util.List;

public interface RankingFunction {

	List<Result> process(String query) throws IOException;
}
