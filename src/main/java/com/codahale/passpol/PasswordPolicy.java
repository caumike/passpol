/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codahale.passpol;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A password policy which validates candidate passwords according to NIST's draft {@code
 * SP-800-63B}, which recommend passwords have a minimum required length, a maximum required
 * length, ad be checked against a list of weak passwords ({@code SP-800-63B 5.1.1.2}).
 * <p>
 * This uses a static list of 10,000 weak passwords downloaded from Carey Li's NBP project.
 *
 * @see <a href="https://pages.nist.gov/800-63-3/">Draft NIST SP-800-63B</a>
 * @see <a href="https://cry.github.io/nbp/">NBP</a>
 */
@Immutable
public class PasswordPolicy implements Predicate<String> {

  private static final String RESOURCE_NAME = "com/codahale/passpol/weak-passwords.txt";

  private final Predicate<String> length;
  private final ImmutableSet<String> weakPasswords;

  /**
   * Creates a {@link PasswordPolicy} with a minimum password length of {@code 8} and a maximum
   * password length of {@code 64}, as recommended in {@code SP-800-63B 5.1.1.2}.
   *
   * @throws IOException if the weak password list cannot be loaded from the classpath
   */
  public PasswordPolicy() throws IOException {
    this(8, 64);
  }

  /**
   * Creates a {@link PasswordPolicy} with the given password length requirements.
   *
   * @param minLength the minimum length of passwords
   * @param maxLength the maximum length of passwords
   * @throws IOException if the weak password list cannot be loaded from the classpath
   */
  public PasswordPolicy(int minLength, int maxLength) throws IOException {
    this.length = s -> {
      final long codePoints = s.codePoints().count();
      return minLength <= codePoints && codePoints <= maxLength;
    };
    this.weakPasswords = Resources.asCharSource(Resources.getResource(RESOURCE_NAME), UTF_8)
                                  .openBufferedStream()
                                  .lines()
                                  .filter(length)
                                  .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Normalizes the given password as Unicode NFKC and returns it as UTF-8 encoded bytes, ready to
   * be passed to a password hashing algorithm like {@code bcrypt}.
   * <p>
   * This is the process recommended in {@code NIST SP-800-63B 5.1.1.2}.
   *
   * @param password an arbitrary string
   * @return a series of bytes suitable for hashing
   */
  public byte[] normalize(@Nonnull String password) {
    return Normalizer.normalize(password, Form.NFKC).getBytes(UTF_8);
  }

  /**
   * Returns {@code true} if the given password is acceptable, {@code false} otherwise.
   *
   * @param password a candidate password
   * @return whether or not {@code password} is acceptable
   */
  @Override
  public boolean test(@Nonnull String password) {
    return length.test(password) && !weakPasswords.contains(password);
  }
}
